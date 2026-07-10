package org.research.causal

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class CausalResponse(val status: String, val message: String)

class ApiClient(private val client: HttpClient) {
    suspend fun fetchCausalData(endpoint: String): CausalResponse {
        return client.get(endpoint).body()
    }
}

fun createDefaultClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}
