package org.research.causal

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiClientIntegrationTest {

    @Test
    fun testUIUXCanCommunicateWithBackendServiceMock() = runTest {
        // Arrange: Mock the backend response
        val mockEngine = MockEngine { request ->
            respond(
                content = """{"status":"success","message":"Integration test data successfully retrieved from Backend"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val apiClient = ApiClient(httpClient)

        // Act: UI calls the ApiClient
        val response = apiClient.fetchCausalData("http://localhost:8000/api/mock-endpoint")

        // Assert: Ensure the response is properly deserialized and valid for the UI to consume
        assertEquals("success", response.status)
        assertEquals("Integration test data successfully retrieved from Backend", response.message)
    }
}
