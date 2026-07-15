import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.research.causal.PocketBaseClient

fun main() = runBlocking {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    
    val pb = PocketBaseClient(client)
    try {
        val auth = pb.authWithPassword("brentmzey4795@gmail.com", "MHD@hyt0arm8dvf5awc")
        println("SUCCESS: " + auth.token)
    } catch (e: Exception) {
        println("FAILED: " + e.message)
        e.printStackTrace()
    }
}
