package org.research.causal

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val client = HttpClient()
    try {
        val authRes = client.post("https://polling-data.pockethost.io/api/collections/_superusers/auth-with-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"identity":"brentmzey4795@gmail.com","password":"MHD@hyt0arm8dvf5awc"}""")
        }
        val token = authRes.bodyAsText().let {
            Regex(""""token":"([^"]+)"""").find(it)?.groupValues?.get(1)
        }
        
        val res = client.get("https://polling-data.pockethost.io/api/collections/poll_results/records?expand=poll_id,candidate_id,geography_id&perPage=300") {
            header(HttpHeaders.Authorization, token)
        }
        println("Status: ${res.status}")
        println("Body: ${res.bodyAsText().take(200)}")
    } catch (e: Exception) {
        println("FAILED: ${e.message}")
    }
}
