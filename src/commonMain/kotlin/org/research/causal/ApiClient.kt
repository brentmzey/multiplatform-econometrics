package org.research.causal

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class CausalResponse(val status: String, val message: String)

class ApiClient(private val client: HttpClient) {
    suspend fun fetchCausalData(endpoint: String): CausalResponse {
        return client.get(endpoint).body()
    }

    suspend fun fetchWorldBankData(): String {
        return client.get("https://api.worldbank.org/v2/country/all/indicator/NY.GDP.MKTP.CD?format=json").bodyAsText()
    }
}

fun createDefaultClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
}
