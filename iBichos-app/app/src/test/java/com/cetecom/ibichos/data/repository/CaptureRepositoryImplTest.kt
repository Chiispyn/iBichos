package com.cetecom.ibichos.data.repository

import com.cetecom.ibichos.domain.model.enums.DangerLevel
import com.cetecom.ibichos.domain.model.enums.InsectCategory
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CaptureRepositoryImplTest {

    // ── Mocks ────────────────────────────────────────────────────────────────
    private val db: FirebaseFirestore         = mockk(relaxed = true)
    private val collection: CollectionReference = mockk(relaxed = true)
    private val query: Query                  = mockk(relaxed = true)
    private val querySnapshot: QuerySnapshot  = mockk(relaxed = true)
    private val documentRef: DocumentReference = mockk(relaxed = true)

    private lateinit var repository: CaptureRepositoryImpl

    private val userId         = "user123"
    private val scientificName = "Lycosa tarantula"
    private val captureId      = "capture456"

    @Before
    fun setUp() {
        repository = CaptureRepositoryImpl(db)
        every { db.collection("captures") } returns collection
    }

    // ════════════════════════════════════════════════════════════════════════
    // getCaptures
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `getCaptures devuelve lista vacia cuando no hay documentos`() = runTest {
        every { collection.whereEqualTo("userId", userId) } returns query
        every { query.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.documents } returns emptyList()

        val result = repository.getCaptures(userId)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getCaptures filtra capturas con status DELETED`() = runTest {
        val docDeleted  = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)
        val docApproved = mockk<com.google.firebase.firestore.DocumentSnapshot>(relaxed = true)

        every { docDeleted.getString("status") }  returns "DELETED"
        every { docApproved.getString("status") } returns "APPROVED"
        every { docDeleted.id }  returns "id1"
        every { docApproved.id } returns "id2"
        // campos mínimos para que toCaptureItem no explote
        every { docDeleted.getString(neq("status")) }  returns ""
        every { docApproved.getString(neq("status")) } returns ""
        every { docDeleted.getDouble(any()) }  returns 0.0
        every { docApproved.getDouble(any()) } returns 0.0
        every { docDeleted.getLong(any()) }    returns 0L
        every { docApproved.getLong(any()) }   returns 0L
        every { docDeleted.getBoolean(any()) }  returns false
        every { docApproved.getBoolean(any()) } returns false
        every { docDeleted.getTimestamp(any()) }  returns null
        every { docApproved.getTimestamp(any()) } returns null

        every { collection.whereEqualTo("userId", userId) } returns query
        every { query.get() } returns Tasks.forResult(querySnapshot)
        every { querySnapshot.documents } returns listOf(docDeleted, docApproved)

        val result = repository.getCaptures(userId)

        assertEquals(1, result.size)
        assertEquals("id2", result.first().id)
    }

    // ════════════════════════════════════════════════════════════════════════
    // hasCaughtInsect
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `hasCaughtInsect retorna true cuando Firestore devuelve documentos`() = runTest {
        val queryByUser      = mockk<Query>(relaxed = true)
        val queryByScientific = mockk<Query>(relaxed = true)
        val queryLimit       = mockk<Query>(relaxed = true)
        val snapshot         = mockk<QuerySnapshot>(relaxed = true)

        every { collection.whereEqualTo("userId", userId) }               returns queryByUser
        every { queryByUser.whereEqualTo("scientificName", scientificName) } returns queryByScientific
        every { queryByScientific.limit(1) }                              returns queryLimit
        every { queryLimit.get() }                                        returns Tasks.forResult(snapshot)
        every { snapshot.isEmpty }                                        returns false

        val result = repository.hasCaughtInsect(userId, scientificName)

        assertTrue(result)
    }

    @Test
    fun `hasCaughtInsect retorna false cuando Firestore no encuentra documentos`() = runTest {
        val queryByUser       = mockk<Query>(relaxed = true)
        val queryByScientific = mockk<Query>(relaxed = true)
        val queryLimit        = mockk<Query>(relaxed = true)
        val snapshot          = mockk<QuerySnapshot>(relaxed = true)

        every { collection.whereEqualTo("userId", userId) }                returns queryByUser
        every { queryByUser.whereEqualTo("scientificName", scientificName) } returns queryByScientific
        every { queryByScientific.limit(1) }                               returns queryLimit
        every { queryLimit.get() }                                         returns Tasks.forResult(snapshot)
        every { snapshot.isEmpty }                                         returns true

        val result = repository.hasCaughtInsect(userId, scientificName)

        assertFalse(result)
    }

    // ════════════════════════════════════════════════════════════════════════
    // saveCapture
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `saveCapture retorna el id del documento creado`() = runTest {
        every { collection.add(any<Map<String, Any?>>()) } returns Tasks.forResult(documentRef)
        every { documentRef.id } returns captureId

        val result = repository.saveCapture(
            userId         = userId,
            imageUrl       = "https://cdn.example.com/bug.jpg",
            insectName     = "Araña Lobo",
            scientificName = scientificName,
            category       = InsectCategory.ARACHNID,
            dangerLevel    = DangerLevel.HARMLESS,
            probability    = 0.85,
            latitude       = 40.4168,
            longitude      = -3.7038,
            xpAwarded      = 150L,
            description    = "Araña de gran tamaño",
            needsReview    = false,
            status         = "APPROVED"
        )

        assertEquals(captureId, result)
    }

    @Test
    fun `saveCapture con coordenadas nulas no lanza excepcion`() = runTest {
        every { collection.add(any<Map<String, Any?>>()) } returns Tasks.forResult(documentRef)
        every { documentRef.id } returns captureId

        val result = repository.saveCapture(
            userId         = userId,
            imageUrl       = "https://cdn.example.com/bug.jpg",
            insectName     = "Araña Lobo",
            scientificName = scientificName,
            category       = InsectCategory.ARACHNID,
            dangerLevel    = DangerLevel.HARMLESS,
            probability    = 0.85,
            latitude       = null,
            longitude      = null,
            xpAwarded      = 150L,
            description    = "Araña de gran tamaño",
            needsReview    = false,
            status         = "APPROVED"
        )

        assertEquals(captureId, result)
    }

    // ════════════════════════════════════════════════════════════════════════
    // deleteCapture
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `deleteCapture actualiza el campo status a DELETED`() = runTest {
        val docRef = mockk<DocumentReference>(relaxed = true)
        every { collection.document(captureId) } returns docRef
        every { docRef.update("status", "DELETED") } returns Tasks.forResult(null)

        repository.deleteCapture(captureId)

        verify { docRef.update("status", "DELETED") }
    }

    // ════════════════════════════════════════════════════════════════════════
    // appealCapture
    // ════════════════════════════════════════════════════════════════════════

    @Test
    fun `appealCapture actualiza status a PENDING_REVIEW y needsReview a true`() = runTest {
        val docRef = mockk<DocumentReference>(relaxed = true)
        every { collection.document(captureId) } returns docRef
        every {
            docRef.update(match<Map<String, Any>> {
                it["status"] == "PENDING_REVIEW" && it["needsReview"] == true
            })
        } returns Tasks.forResult(null)

        repository.appealCapture(captureId)

        verify {
            docRef.update(match<Map<String, Any>> {
                it["status"] == "PENDING_REVIEW" && it["needsReview"] == true
            })
        }
    }
}
