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
    private val baseUrl: String = "https://econometrics-broker.pockethost.io" 
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

    suspend fun createRecord(collectionName: String, data: Map<String, Any>): JsonObject {
        val response = client.post("$baseUrl/api/collections/$collectionName/records") {
            contentType(ContentType.Application.Json)
            if (authToken != null) {
                header(HttpHeaders.Authorization, authToken)
            }
            // Use Gson or raw string for Ktor serialization. 
            // Ktor's Kotlinx-serialization needs explicit handling for Map<String, Any>, 
            // so we'll build a raw JSON string for simplicity.
            val jsonBody = buildString {
                append("{")
                val entries = data.entries.toList()
                for (i in entries.indices) {
                    val entry = entries[i]
                    append("\"${entry.key}\":")
                    when (val v = entry.value) {
                        is String -> append("\"${v.replace("\"", "\\\"")}\"")
                        is List<*> -> {
                            append("[")
                            val list = v.toList()
                            for (j in list.indices) {
                                val item = list[j]
                                if (item is String) append("\"${item.replace("\"", "\\\"")}\"")
                                else if (item is List<*>) {
                                    append("[")
                                    val sublist = item.toList()
                                    for (k in sublist.indices) {
                                        append(sublist[k].toString())
                                        if (k < sublist.size - 1) append(",")
                                    }
                                    append("]")
                                }
                                else append(item.toString())
                                if (j < list.size - 1) append(",")
                            }
                            append("]")
                        }
                        else -> append(v.toString())
                    }
                    if (i < entries.size - 1) append(",")
                }
                append("}")
            }
            setBody(jsonBody)
        }

        if (!response.status.isSuccess()) {
            throw Exception("Failed to create record in $collectionName: ${response.status.description}")
        }
        return response.body()
    }
    
    fun logout() {
        this.authToken = null
    }
}
