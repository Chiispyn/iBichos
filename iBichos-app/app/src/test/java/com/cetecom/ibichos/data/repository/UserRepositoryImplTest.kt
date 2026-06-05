package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.domain.repository.EventRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    // ── Mocks ────────────────────────────────────────────────────────────────
    private val db: FirebaseFirestore         = mockk(relaxed = true)
    private val eventRepo: EventRepository    = mockk(relaxed = true)

    private val usersCollection: CollectionReference   = mockk(relaxed = true)
    private val capturesCollection: CollectionReference = mockk(relaxed = true)
    private val userDocRef: DocumentReference          = mockk(relaxed = true)
    private val userDocSnap: DocumentSnapshot          = mockk(relaxed = true)
    private val captureQuery: Query                    = mockk(relaxed = true)
    private val captureSnapshot: QuerySnapshot         = mockk(relaxed = true)

    private lateinit var repository: UserRepositoryImpl

    private val uid       = "user123"
    private val avatarUrl = "https://cdn.example.com/avatar.jpg"

    @Before
    fun setUp() {
        repository = UserRepositoryImpl(db, eventRepo)

        every { db.collection("users") }    returns usersCollection
        every { db.collection("captures") } returns capturesCollection
        every { usersCollection.document(uid) } returns userDocRef
        every { userDocRef.get() }              returns Tasks.forResult(userDocSnap)

        // Perfil base sin shadow ban
        every { userDocSnap.getBoolean("isShadowBanned") } returns false
        every { userDocSnap.exists() } returns true
        every { userDocSnap.id }       returns uid
        every { userDocSnap.get("gamification") } returns mapOf(
            "xp"                 to 0L,
            "level"              to "CASUAL",
            "uniqueInsectsCount" to 0L,
            "categoryCounts"     to emptyMap<String, Long>(),
            "medals"             to emptyList<String>(),
            "medalsEarnedAt"     to emptyMap<String, Long>(),
            "levelUpAt"          to emptyMap<String, Long>()
        )
        every { userDocSnap.getString(any()) }  returns ""
        every { userDocSnap.getLong(any()) }    returns 0L

        // captures por defecto vacío
        every { capturesCollection.whereEqualTo("userId", uid) } returns captureQuery
        every { captureQuery.get() } returns Tasks.forResult(captureSnapshot)
        every { captureSnapshot.size() } returns 0
    }

    // ════════════════════════════════════════════════════════════════════════
    // getUserProfile
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `getUserProfile retorna perfil con uid correcto`() = runTest {
        val result = repository.getUserProfile(uid)
        assertEquals(uid, result.uid)
    }

    @Test
    fun `getUserProfile incluye totalCaptures del conteo de Firestore`() = runTest {
        every { captureSnapshot.size() } returns 5

        val result = repository.getUserProfile(uid)

        assertEquals(5, result.totalCaptures)
    }

    // ════════════════════════════════════════════════════════════════════════
    // updateAvatar
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `updateAvatar actualiza el campo avatarUrl y retorna la url`() = runTest {
        every { userDocRef.update("avatarUrl", avatarUrl) } returns Tasks.forResult(null)

        val result = repository.updateAvatar(uid, avatarUrl)

        assertEquals(avatarUrl, result)
        verify { userDocRef.update("avatarUrl", avatarUrl) }
    }

    // ════════════════════════════════════════════════════════════════════════
    // incrementXp
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `incrementXp no actualiza nada si el usuario esta shadowbanned`() = runTest {
        every { userDocSnap.getBoolean("isShadowBanned") } returns true

        repository.incrementXp(uid, 150L)

        verify(exactly = 0) { userDocRef.update(any<Map<String, Any>>()) }
    }

    @Test
    fun `incrementXp actualiza xp correctamente si el usuario no esta shadowbanned`() = runTest {
        every { userDocSnap.getBoolean("isShadowBanned") } returns false
        every { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        repository.incrementXp(uid, 150L)

        verify {
            userDocRef.update(match<Map<String, Any>> {
                it["gamification.xp"] == 150L && it["xp"] == 150L
            })
        }
    }

    @Test
    fun `incrementXp no registra levelUp si el nivel no cambia`() = runTest {
        every { userDocSnap.getBoolean("isShadowBanned") } returns false
        every { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        // Con 50 XP el nivel sigue siendo CASUAL
        repository.incrementXp(uid, 50L)

        coVerify(exactly = 0) { eventRepo.logLevelUp(any(), any(), any(), any()) }
    }

    @Test
    fun `incrementXp registra evento logLevelUp cuando el nivel sube`() = runTest {
        // Simulamos usuario con 490 XP en nivel CASUAL, sumamos 150 → supera 500 → AFICIONADO
        every { userDocSnap.get("gamification") } returns mapOf(
            "xp"                 to 490L,
            "level"              to "CASUAL",
            "uniqueInsectsCount" to 0L,
            "categoryCounts"     to emptyMap<String, Long>(),
            "medals"             to emptyList<String>(),
            "medalsEarnedAt"     to emptyMap<String, Long>(),
            "levelUpAt"          to emptyMap<String, Long>()
        )
        every { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)
        coEvery { eventRepo.logLevelUp(any(), any(), any(), any()) } just Runs

        repository.incrementXp(uid, 150L)

        coVerify { eventRepo.logLevelUp(uid, "CASUAL", any(), 640L) }
    }

    @Test
    fun `incrementXp crea documento si el usuario no existe en Firestore`() = runTest {
        every { userDocSnap.exists() }                  returns false
        every { userDocSnap.getBoolean("isShadowBanned") } returns false
        every { userDocRef.set(any<Map<String, Any>>()) } returns Tasks.forResult(null)

        repository.incrementXp(uid, 150L)

        verify { userDocRef.set(any<Map<String, Any>>()) }
        verify(exactly = 0) { userDocRef.update(any<Map<String, Any>>()) }
    }

    // ════════════════════════════════════════════════════════════════════════
    // getTopUsersByXp
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `getTopUsersByXp filtra usuarios shadowbanned`() = runTest {
        val queryOrdered = mockk<Query>(relaxed = true)
        val queryLimited = mockk<Query>(relaxed = true)
        val snapshot     = mockk<QuerySnapshot>(relaxed = true)

        val docNormal  = buildUserDoc("u1", isShadowBanned = false)
        val docBanned  = buildUserDoc("u2", isShadowBanned = true)

        every { usersCollection.orderBy("xp", Query.Direction.DESCENDING) } returns queryOrdered
        every { queryOrdered.limit(50L) }  returns queryLimited
        every { queryLimited.get() }       returns Tasks.forResult(snapshot)
        every { snapshot.documents }       returns listOf(docNormal, docBanned)

        val result = repository.getTopUsersByXp(50)

        assertEquals(1, result.size)
        assertEquals("u1", result.first().uid)
    }

    // ════════════════════════════════════════════════════════════════════════
    // getTopUsersByUniqueInsects
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `getTopUsersByUniqueInsects filtra usuarios shadowbanned`() = runTest {
        val queryOrdered = mockk<Query>(relaxed = true)
        val queryLimited = mockk<Query>(relaxed = true)
        val snapshot     = mockk<QuerySnapshot>(relaxed = true)

        val docNormal = buildUserDoc("u1", isShadowBanned = false)
        val docBanned = buildUserDoc("u2", isShadowBanned = true)

        every { usersCollection.orderBy("uniqueInsectsCount", Query.Direction.DESCENDING) } returns queryOrdered
        every { queryOrdered.limit(50L) } returns queryLimited
        every { queryLimited.get() }      returns Tasks.forResult(snapshot)
        every { snapshot.documents }      returns listOf(docNormal, docBanned)

        val result = repository.getTopUsersByUniqueInsects(50)

        assertEquals(1, result.size)
        assertEquals("u1", result.first().uid)
    }

    // ════════════════════════════════════════════════════════════════════════
    // getTopUsersByMedals
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `getTopUsersByMedals ordena por cantidad de medallas y filtra shadowbanned`() = runTest {
        val queryOrdered = mockk<Query>(relaxed = true)
        val queryLimited = mockk<Query>(relaxed = true)
        val snapshot     = mockk<QuerySnapshot>(relaxed = true)

        val docPocos   = buildUserDoc("u1", isShadowBanned = false, medals = listOf("M1"))
        val docMuchos  = buildUserDoc("u2", isShadowBanned = false, medals = listOf("M1", "M2", "M3"))
        val docBanned  = buildUserDoc("u3", isShadowBanned = true,  medals = listOf("M1", "M2", "M3", "M4"))

        every { usersCollection.orderBy("xp", Query.Direction.DESCENDING) } returns queryOrdered
        every { queryOrdered.limit(100L) } returns queryLimited
        every { queryLimited.get() }       returns Tasks.forResult(snapshot)
        every { snapshot.documents }       returns listOf(docPocos, docMuchos, docBanned)

        val result = repository.getTopUsersByMedals(2)

        assertEquals(2, result.size)
        assertEquals("u2", result.first().uid)  // más medallas primero
        assertEquals("u1", result[1].uid)
    }

    // ════════════════════════════════════════════════════════════════════════
    // unlockMedalsAndIncrementUnique
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `unlockMedalsAndIncrementUnique no hace nada si todo esta vacio`() = runTest {
        repository.unlockMedalsAndIncrementUnique(
            uid               = uid,
            medalsToUnlock    = emptyList(),
            isNewInsect       = false,
            incrementCategory = null
        )

        verify(exactly = 0) { userDocRef.update(any<Map<String, Any>>()) }
    }

    @Test
    fun `unlockMedalsAndIncrementUnique no actualiza nada si usuario esta shadowbanned`() = runTest {
        every { userDocSnap.getBoolean("isShadowBanned") } returns true

        repository.unlockMedalsAndIncrementUnique(
            uid               = uid,
            medalsToUnlock    = listOf("FIRST_CAPTURE"),
            isNewInsect       = true,
            incrementCategory = "ARACHNID"
        )

        verify(exactly = 0) { userDocRef.update(any<Map<String, Any>>()) }
    }

    @Test
    fun `unlockMedalsAndIncrementUnique registra evento logMedalUnlocked por cada medalla`() = runTest {
        every { userDocRef.update(any<Map<String, Any>>()) } returns Tasks.forResult(null)
        every { userDocSnap.get("gamification") } returns mapOf(
            "xp" to 150L,
            "medals" to emptyList<String>()
        )
        coEvery { eventRepo.logMedalUnlocked(any(), any(), any()) } just Runs

        repository.unlockMedalsAndIncrementUnique(
            uid               = uid,
            medalsToUnlock    = listOf("FIRST_CAPTURE", "BRAVE_HUNTER"),
            isNewInsect       = true,
            incrementCategory = "ARACHNID"
        )

        coVerify(exactly = 2) { eventRepo.logMedalUnlocked(uid, any(), any()) }
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helper
    // ════════════════════════════════════════════════════════════════════════

    private fun buildUserDoc(
        uid: String,
        isShadowBanned: Boolean,
        medals: List<String> = emptyList()
    ): DocumentSnapshot = mockk(relaxed = true) {
        every { id }                            returns uid
        every { getBoolean("isShadowBanned") }  returns isShadowBanned
        every { get("gamification") }           returns mapOf(
            "xp"                 to 0L,
            "level"              to "CASUAL",
            "uniqueInsectsCount" to 0L,
            "categoryCounts"     to emptyMap<String, Long>(),
            "medals"             to medals,
            "medalsEarnedAt"     to emptyMap<String, Long>(),
            "levelUpAt"          to emptyMap<String, Long>()
        )
        every { getString(any()) }  returns ""
        every { getLong(any()) }    returns 0L
    }
}
