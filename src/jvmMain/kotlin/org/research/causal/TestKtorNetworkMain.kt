package org.research.causal

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.research.causal.db.*

fun main() = runBlocking {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }
    val pb = PocketBaseClient(client)
    pb.authWithPassword("brentmzey4795@gmail.com", "MHD@hyt0arm8dvf5awc")
    try {
       val records = pb.getRecords(collectionName = "poll_results", expand = "poll_id,candidate_id,geography_id", perPage = 300)
       println("Success! Fetched ${records.items.size}")
    } catch (e: Exception) {
       println("FAILED: ${e.message}")
    }
}
