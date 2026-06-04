package com.cetecom.ibichos.domain.usecase.capture

import com.cetecom.ibichos.domain.model.GamificationData
import com.cetecom.ibichos.domain.model.UserProfile
import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.cetecom.ibichos.domain.model.enums.MedalInfo
import com.cetecom.ibichos.domain.repository.CaptureRepository
import com.cetecom.ibichos.domain.repository.EventRepository
import com.cetecom.ibichos.domain.repository.UserRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProcessCaptureUseCaseTest {

    // ── Mocks ────────────────────────────────────────────────────────────────
    private val captureRepository: CaptureRepository = mockk(relaxed = true)
    private val userRepository: UserRepository       = mockk(relaxed = true)
    private val eventRepository: EventRepository     = mockk(relaxed = true)

    private lateinit var useCase: ProcessCaptureUseCase

    // ── Datos base reutilizables ─────────────────────────────────────────────
    private val uid            = "user123"
    private val imageUrl       = "https://cdn.example.com/bug.jpg"
    private val insectName     = "Araña Lobo"
    private val scientificName = "Lycosa tarantula"
    private val category       = InsectCategory.ARACHNID
    private val dangerLevel    = DangerLevel.HARMLESS
    private val lat            = 40.4168
    private val lon            = -3.7038
    private val description    = "Araña de gran tamaño."

    /** Perfil de usuario base (sin capturas previas, sin medallas). */
    private val baseUserProfile = UserProfile(
        uid          = uid,
        displayName  = "Tester",
        gamification = GamificationData(
            xp                 = 0L,
            uniqueInsectsCount = 0,
            medals             = emptyList(),
            categoryCounts     = emptyMap()
        )
    )

    @Before
    fun setUp() {
        useCase = ProcessCaptureUseCase(captureRepository, userRepository, eventRepository)
        // Por defecto, el usuario NO ha capturado antes esta especie
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns false
        // Por defecto, el perfil del usuario no tiene medallas ni capturas previas
        coEvery { userRepository.getUserProfile(uid) } returns baseUserProfile
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 1 — MODERACIÓN: umbrales de confianza
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `dado probability menor a 0_40, retorna LowConfidence y no guarda nada`() = runTest {
        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.39,
            lat = lat, lon = lon, description = description
        )

        // Debe retornar LowConfidence con la probabilidad exacta
        assertTrue(result is CaptureResult.LowConfidence)
        assertEquals(0.39, (result as CaptureResult.LowConfidence).probability, 0.001)

        // No debe tocar ningún repositorio
        coVerify(exactly = 0) { captureRepository.saveCapture(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { userRepository.incrementXp(any(), any()) }
    }

    @Test
    fun `dado probability exactamente 0_40, es valido y retorna Success`() = runTest {
        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.40,
            lat = lat, lon = lon, description = description
        )

        assertTrue(result is CaptureResult.Success)
    }

    @Test
    fun `dado probability entre 0_40 y 0_75, guarda con status PENDING_REVIEW`() = runTest {
        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.60,
            lat = lat, lon = lon, description = description
        )

        coVerify {
            captureRepository.saveCapture(
                userId        = uid,
                imageUrl      = imageUrl,
                insectName    = insectName,
                scientificName = scientificName,
                category      = category,
                dangerLevel   = dangerLevel,
                probability   = 0.60,
                latitude      = lat,
                longitude     = lon,
                xpAwarded     = any(),
                description   = description,
                needsReview   = true,
                status        = "PENDING_REVIEW"
            )
        }
    }

    @Test
    fun `dado probability igual o mayor a 0_75, guarda con status APPROVED`() = runTest {
        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.75,
            lat = lat, lon = lon, description = description
        )

        coVerify {
            captureRepository.saveCapture(
                userId        = uid,
                imageUrl      = imageUrl,
                insectName    = insectName,
                scientificName = scientificName,
                category      = category,
                dangerLevel   = dangerLevel,
                probability   = 0.75,
                latitude      = lat,
                longitude     = lon,
                xpAwarded     = any(),
                description   = description,
                needsReview   = false,
                status        = "APPROVED"
            )
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 2 — GAMIFICACIÓN: XP
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `especie nueva otorga 150 XP`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns false

        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        coVerify { userRepository.incrementXp(uid, 150L) }
    }

    @Test
    fun `especie repetida otorga solo 20 XP`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns true

        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        coVerify { userRepository.incrementXp(uid, 20L) }
    }

    @Test
    fun `el mensaje de exito incluye el XP otorgado`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns false

        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        val msg = (result as CaptureResult.Success).message
        assertTrue("El mensaje debe incluir +150 XP", msg.contains("+150 XP"))
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 3 — GAMIFICACIÓN: Medallas
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `primera especie nueva desbloquea medalla FIRST_CAPTURE`() = runTest {
        // Usuario con 0 capturas únicas → al capturar 1 especie nueva, se desbloquea FIRST_CAPTURE
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns false
        coEvery { userRepository.getUserProfile(uid) } returns baseUserProfile // uniqueInsectsCount = 0

        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        val msg = (result as CaptureResult.Success).message
        assertTrue("Debe mencionar la medalla Primer Avistamiento", msg.contains("Primer Avistamiento"))

        coVerify {
            userRepository.unlockMedalsAndIncrementUnique(
                uid               = uid,
                medalsToUnlock    = match { it.contains(MedalInfo.FIRST_CAPTURE.id) },
                isNewInsect       = true,
                incrementCategory = category.name
            )
        }
    }

    @Test
    fun `capturar insecto VENOMOUS desbloquea medalla BRAVE_HUNTER`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns false
        coEvery { userRepository.getUserProfile(uid) } returns baseUserProfile

        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = DangerLevel.VENOMOUS, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        val msg = (result as CaptureResult.Success).message
        assertTrue("Debe mencionar la medalla Cazador Valiente", msg.contains("Cazador Valiente"))
    }

    @Test
    fun `captura repetida no desbloquea medalla FIRST_CAPTURE si ya la tiene`() = runTest {
        val userWithMedal = baseUserProfile.copy(
            gamification = baseUserProfile.gamification.copy(
                medals             = listOf(MedalInfo.FIRST_CAPTURE.id),
                uniqueInsectsCount = 1
            )
        )
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns true
        coEvery { userRepository.getUserProfile(uid) } returns userWithMedal

        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        coVerify {
            userRepository.unlockMedalsAndIncrementUnique(
                uid               = uid,
                medalsToUnlock    = match { !it.contains(MedalInfo.FIRST_CAPTURE.id) },
                isNewInsect       = false,
                incrementCategory = category.name
            )
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 4 — EVENTOS: registro de especie descubierta
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `especie nueva registra evento logSpeciesDiscovered`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns false

        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        coVerify {
            eventRepository.logSpeciesDiscovered(
                userId         = uid,
                scientificName = scientificName,
                insectName     = insectName,
                category       = category.name,
                xpAtEvent      = any()
            )
        }
    }

    @Test
    fun `especie repetida NO registra evento logSpeciesDiscovered`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns true

        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        coVerify(exactly = 0) {
            eventRepository.logSpeciesDiscovered(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `fallo en logSpeciesDiscovered no interrumpe el flujo (runCatching)`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns false
        coEvery {
            eventRepository.logSpeciesDiscovered(any(), any(), any(), any(), any())
        } throws RuntimeException("Firestore caído")

        // No debe lanzar excepción al exterior
        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        assertTrue(result is CaptureResult.Success)
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 5 — MENSAJE DE RETORNO
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `mensaje de especie nueva incluye texto NUEVA ESPECIE DESCUBIERTA`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns false

        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        val msg = (result as CaptureResult.Success).message
        assertTrue(msg.contains("NUEVA ESPECIE DESCUBIERTA"))
    }

    @Test
    fun `mensaje de especie repetida NO incluye texto NUEVA ESPECIE DESCUBIERTA`() = runTest {
        coEvery { captureRepository.hasCaughtInsect(uid, scientificName) } returns true

        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = lat, lon = lon, description = description
        )

        val msg = (result as CaptureResult.Success).message
        assertFalse(msg.contains("NUEVA ESPECIE DESCUBIERTA"))
    }

    @Test
    fun `mensaje de captura pendiente incluye aviso de verificacion`() = runTest {
        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.60, // entre 0.40 y 0.75
            lat = lat, lon = lon, description = description
        )

        val msg = (result as CaptureResult.Success).message
        assertTrue(msg.contains("Pendiente de verificación"))
    }

    @Test
    fun `mensaje incluye el porcentaje de confianza redondeado`() = runTest {
        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.876,
            lat = lat, lon = lon, description = description
        )

        val msg = (result as CaptureResult.Success).message
        assertTrue("Debe mostrar 87% de confianza", msg.contains("87%"))
    }

    // ════════════════════════════════════════════════════════════════════════
    // BLOQUE 6 — CASOS BORDE
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `lat y lon nulas se pasan correctamente al repositorio`() = runTest {
        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.80,
            lat = null, lon = null, description = description
        )

        coVerify {
            captureRepository.saveCapture(
                userId        = uid,
                imageUrl      = any(),
                insectName    = any(),
                scientificName = any(),
                category      = any(),
                dangerLevel   = any(),
                probability   = any(),
                latitude      = null,
                longitude     = null,
                xpAwarded     = any(),
                description   = any(),
                needsReview   = any(),
                status        = any()
            )
        }
    }

    @Test
    fun `probability exactamente 0_399 retorna LowConfidence`() = runTest {
        val result = useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.399,
            lat = lat, lon = lon, description = description
        )

        assertTrue(result is CaptureResult.LowConfidence)
    }

    @Test
    fun `probability exactamente 0_749 guarda como PENDING_REVIEW`() = runTest {
        useCase(
            uid = uid, imageUrl = imageUrl, insectName = insectName,
            scientificName = scientificName, category = category,
            dangerLevel = dangerLevel, probability = 0.749,
            lat = lat, lon = lon, description = description
        )

        coVerify {
            captureRepository.saveCapture(
                userId = uid, imageUrl = any(), insectName = any(),
                scientificName = any(), category = any(), dangerLevel = any(),
                probability = any(), latitude = any(), longitude = any(),
                xpAwarded = any(), description = any(),
                needsReview = true, status = "PENDING_REVIEW"
            )
        }
    }
}
