package org.research.causal

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Serializable
data class PocketBaseAuthResponse(
    val token: String,
    val record: JsonObject
)

@Serializable
data class PocketBaseRecordList(
    val page: Int,
    val perPage: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<JsonObject>
)

class PocketBaseClient(
    private val client: HttpClient,
    private val baseUrl: String = "https://your-instance.pockethost.io" // TODO: Replace with real URL
) {
    private var authToken: String? = null

    suspend fun authWithPassword(email: String, password: String): PocketBaseAuthResponse {
        val response = client.post("$baseUrl/api/collections/users/auth-with-password") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "identity" to email,
                    "password" to password
                )
            )
        }

        if (!response.status.isSuccess()) {
            throw Exception("Failed to authenticate with PocketBase: ${response.status.description}")
        }

        val authResponse: PocketBaseAuthResponse = response.body()
        this.authToken = authResponse.token
        return authResponse
    }

    suspend fun getRecords(collectionName: String): PocketBaseRecordList {
        val response = client.get("$baseUrl/api/collections/$collectionName/records") {
            if (authToken != null) {
                header(HttpHeaders.Authorization, authToken)
            }
        }
        
        if (!response.status.isSuccess()) {
            throw Exception("Failed to fetch records from $collectionName: ${response.status.description}")
        }
        
        return response.body()
    }
    
    fun logout() {
        this.authToken = null
    }
}
