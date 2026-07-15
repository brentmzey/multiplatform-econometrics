import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.research.causal.*
import org.research.causal.db.*

fun main() = runBlocking {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }
    val pb = PocketBaseClient(client)
    pb.authWithPassword("brentmzey4795@gmail.com", "MHD@hyt0arm8dvf5awc")
    val repo = PollingRepository(pb, JvmDatabaseDriverFactory())
    val polls = repo.getPolls(forceRefresh = true)
    println("Fetched ${polls.size} polls!")
}
