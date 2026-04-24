package com.cetecom.ibichos.data.repository

import android.net.Uri
import com.cetecom.ibichos.domain.model.GamificationData
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.model.enums.Gender
import com.cetecom.ibichos.domain.model.enums.UserLevel
import com.cetecom.ibichos.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Implementación de [UserRepository] usando Firestore + Firebase Storage.
 *
 * Estructura de Firestore:
 *   users/{uid}: {
 *     displayName, email, region, city, birthDate, gender, avatarUrl,
 *     totalCaptures, strikes, isShadowBanned,
 *     gamification: { xp, level, uniqueInsectsCount, categoryCounts, medals, medalsEarnedAt, levelUpAt }
 *   }
 */
class UserRepositoryImpl(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val eventRepo: EventRepositoryImpl = EventRepositoryImpl()
) : UserRepository {

    override suspend fun getUserProfile(uid: String): UserProfile {
        val doc = db.collection("users").document(uid).get().await()

        // Contar capturas del usuario desde la colección 'captures'
        val capturesResult = db.collection("captures")
            .whereEqualTo("userId", uid)
            .get()
            .await()

        return mapDocumentToUserProfile(doc, captureCount = capturesResult.size())
    }

    override suspend fun updateAvatar(uid: String, uri: Uri): String {
        // Evitamos Firebase Storage para no incurrir en gastos GCP.
        // Se guarda la URI local como String en Firestore.
        val localUrl = uri.toString()
        db.collection("users").document(uid)
            .update("avatarUrl", localUrl)
            .await()
        return localUrl
    }

    override suspend fun incrementXp(uid: String, xpGain: Long) {
        val userRef = db.collection("users").document(uid)
        val doc = userRef.get().await()

        // 🛡️ Verificar Shadowban: Si el usuario está baneado, no gana XP
        val isShadowBanned = doc.getBoolean("isShadowBanned") ?: false
        if (isShadowBanned) return

        // Leer XP actual desde el sub-documento gamification
        @Suppress("UNCHECKED_CAST")
        val gamificationMap = doc.get("gamification") as? Map<String, Any> ?: emptyMap()
        val currentXp = ((gamificationMap["xp"] as? Long) ?: 0L) + xpGain
        val currentLevelStr = (gamificationMap["level"] as? String) ?: UserLevel.CASUAL.name

        val newLevel = UserLevel.fromXp(currentXp)
        val newLevelStr = newLevel.name

        if (doc.exists()) {
            val updates = mutableMapOf<String, Any>(
                "gamification.xp"    to currentXp,
                "gamification.level" to newLevelStr,
                "xp"                 to currentXp
            )
            if (newLevelStr != currentLevelStr) {
                updates["gamification.levelUpAt.$newLevelStr"] = System.currentTimeMillis()
                // 📝 Registrar evento de subida de nivel para analíticas históricas
                runCatching {
                    eventRepo.logLevelUp(
                        userId = uid,
                        previousLevel = currentLevelStr,
                        newLevel = newLevelStr,
                        xpAtEvent = currentXp
                    )
                }
            }
            userRef.update(updates).await()
        } else {
            // El documento no existe aún: se crea con mapa anidado real (no dot-notation)
            // para que Firestore lo entienda como sub-documento desde el inicio.
            val levelUpAtMap = if (newLevelStr != UserLevel.CASUAL.name) {
                mapOf(newLevelStr to System.currentTimeMillis())
            } else emptyMap()

            userRef.set(
                mapOf(
                    "xp"                 to currentXp,
                    "uniqueInsectsCount" to 0,
                    "medalsCount"        to 0,
                    "strikes"            to 0,
                    "isShadowBanned"     to false,
                    "gamification"       to mapOf(
                        "xp"                 to currentXp,
                        "level"              to newLevelStr,
                        "uniqueInsectsCount" to 0,
                        "categoryCounts"     to emptyMap<String, Int>(),
                        "medals"             to emptyList<String>(),
                        "medalsEarnedAt"     to emptyMap<String, Long>(),
                        "levelUpAt"          to levelUpAtMap
                    )
                )
            ).await()
        }
    }

    override suspend fun getTopUsersByXp(limit: Int): List<UserProfile> {
        // Usamos el campo raíz 'xp' (denormalizado) para aprovechar el índice automático de Firestore.
        // El campo canónico vive en gamification.xp; este es solo para ordering eficiente.
        val snapshot = db.collection("users")
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        return snapshot.documents
            .filter { it.getBoolean("isShadowBanned") != true }
            .map { mapDocumentToUserProfileFast(it) }
    }

    override suspend fun getTopUsersByUniqueInsects(limit: Int): List<UserProfile> {
        // Igual: usamos 'uniqueInsectsCount' en raíz para el índice automático.
        val snapshot = db.collection("users")
            .orderBy("uniqueInsectsCount", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        return snapshot.documents
            .filter { it.getBoolean("isShadowBanned") != true }
            .map { mapDocumentToUserProfileFast(it) }
    }

    override suspend fun getTopUsersByMedals(limit: Int): List<UserProfile> {
        // Firestore no permite ordenar nativamente por longitud de arreglos (medals.size).
        // Traemos los 100 más activos por XP (campo raíz) y ordenamos en RAM por cantidad de medallas.
        val snapshot = db.collection("users")
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(100L)
            .get()
            .await()

        return snapshot.documents
            .filter { it.getBoolean("isShadowBanned") != true }
            .map { mapDocumentToUserProfileFast(it) }
            .sortedByDescending { it.gamification.medals.size }
            .take(limit)
    }

    override suspend fun unlockMedalsAndIncrementUnique(
        uid: String,
        medalsToUnlock: List<String>,
        isNewInsect: Boolean,
        incrementCategory: String?
    ) {
        if (medalsToUnlock.isEmpty() && !isNewInsect && incrementCategory == null) return

        val userRef = db.collection("users").document(uid)
        val doc = userRef.get().await()

        // 🛡️ Verificar Shadowban: Si el usuario está baneado, no desbloquea logros ni suma especies
        val isShadowBanned = doc.getBoolean("isShadowBanned") ?: false
        if (isShadowBanned) return

        val updates = mutableMapOf<String, Any>()

        if (medalsToUnlock.isNotEmpty()) {
            updates["gamification.medals"] = FieldValue.arrayUnion(*medalsToUnlock.toTypedArray())
            val now = System.currentTimeMillis()
            medalsToUnlock.forEach { medal ->
                updates["gamification.medalsEarnedAt.$medal"] = now
            }
            // Denormalización: medalsCount en raíz para ordenar por medallas sin índice compuesto
            updates["medalsCount"] = FieldValue.increment(medalsToUnlock.size.toLong())
        }
        if (isNewInsect) {
            updates["gamification.uniqueInsectsCount"] = FieldValue.increment(1)
            // Denormalización: uniqueInsectsCount en raíz para ranking
            updates["uniqueInsectsCount"] = FieldValue.increment(1)
        }
        if (incrementCategory != null) {
            updates["gamification.categoryCounts.$incrementCategory"] = FieldValue.increment(1)
        }

        userRef.update(updates).await()

        // 📝 Registrar eventos para analíticas históricas (append-only, no bloquean el flujo)
        val currentXp = runCatching {
            val doc = userRef.get().await()
            @Suppress("UNCHECKED_CAST")
            val gamMap = doc.get("gamification") as? Map<String, Any> ?: emptyMap()
            (gamMap["xp"] as? Long) ?: 0L
        }.getOrDefault(0L)

        medalsToUnlock.forEach { medal ->
            runCatching {
                eventRepo.logMedalUnlocked(uid, medal, currentXp)
            }
        }
        if (isNewInsect && incrementCategory != null) {
            // El insectName y scientificName no están disponibles aquí;
            // se registran en CameraViewModel antes de llamar a este método.
            // Este log registra la categoría al menos para métricas de biodiversidad.
            runCatching {
                eventRepo.logSpeciesDiscovered(
                    userId = uid,
                    scientificName = "",  // completado desde CameraViewModel
                    insectName = "",
                    category = incrementCategory,
                    xpAtEvent = currentXp
                )
            }
        }
    }

    // -----------------------------------------------------------------------
    // Funciones auxiliares de mapeo Firestore → dominio
    // -----------------------------------------------------------------------

    /**
     * Mapeo completo: usa el conteo de capturas proporcionado externamente.
     * (Se llama cuando ya tenemos el número exacto de capturas del usuario.)
     */
    private fun mapDocumentToUserProfile(doc: DocumentSnapshot, captureCount: Int): UserProfile {
        return buildUserProfile(doc, captureCount)
    }

    /**
     * Mapeo rápido para listados (ranking). No consulta la colección 'captures',
     * usa totalCaptures = 0 para evitar N+1 queries.
     */
    private fun mapDocumentToUserProfileFast(doc: DocumentSnapshot): UserProfile {
        return buildUserProfile(doc, captureCount = 0)
    }

    /** Construye un [UserProfile] desde un [DocumentSnapshot] de Firestore. */
    @Suppress("UNCHECKED_CAST")
    private fun buildUserProfile(doc: DocumentSnapshot, captureCount: Int): UserProfile {
        val gamificationMap = doc.get("gamification") as? Map<String, Any> ?: emptyMap()

        val gamification = GamificationData(
            xp                 = (gamificationMap["xp"] as? Long) ?: 0L,
            level              = runCatching {
                UserLevel.valueOf((gamificationMap["level"] as? String) ?: UserLevel.CASUAL.name)
            }.getOrDefault(UserLevel.CASUAL),
            uniqueInsectsCount = ((gamificationMap["uniqueInsectsCount"] as? Long)?.toInt()) ?: 0,
            categoryCounts     = (gamificationMap["categoryCounts"] as? Map<String, Long>)
                ?.mapValues { it.value.toInt() } ?: emptyMap(),
            medals             = (gamificationMap["medals"] as? List<String>) ?: emptyList(),
            medalsEarnedAt     = (gamificationMap["medalsEarnedAt"] as? Map<String, Long>) ?: emptyMap(),
            levelUpAt          = (gamificationMap["levelUpAt"] as? Map<String, Long>) ?: emptyMap()
        )

        val gender = runCatching {
            Gender.valueOf(doc.getString("gender") ?: Gender.UNSPECIFIED.name)
        }.getOrDefault(Gender.UNSPECIFIED)

        return UserProfile(
            uid           = doc.id,
            displayName   = doc.getString("displayName") ?: "Usuario",
            email         = doc.getString("email") ?: "",
            region        = doc.getString("region") ?: "",
            city          = doc.getString("city") ?: "",
            birthDate     = doc.getString("birthDate") ?: "",
            gender        = gender,
            avatarUrl     = doc.getString("avatarUrl"),
            totalCaptures = captureCount,
            strikes       = (doc.getLong("strikes")?.toInt()) ?: 0,
            isShadowBanned = doc.getBoolean("isShadowBanned") ?: false,
            gamification  = gamification
        )
    }
}
