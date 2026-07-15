package org.research.causal
import kotlinx.coroutines.runBlocking
import org.research.causal.db.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

fun main() = runBlocking {
    try {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
        val pb = PocketBaseClient(client)
        pb.authWithPassword("brentmzey4795@gmail.com", "MHD@hyt0arm8dvf5awc")
        val repo = PollingRepository(pb, JvmDatabaseDriverFactory())
        val polls = repo.getPolls(forceRefresh = true)
        println("SUCCESS: Fetched ${polls.size} polls through full Repository abstraction!")
    } catch (e: Exception) {
        println("ERROR: ${e.message}")
        e.printStackTrace()
    }
}
