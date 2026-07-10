package org.research.causal

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class WorldBankIntegrationTest {

    @Test
    fun testFetchRemoteWorldBankData() = runTest {
        val client = createDefaultClient()
        val apiClient = ApiClient(client)
        
        try {
            val responseText = apiClient.fetchWorldBankData()
            // Just verifying that the actual HTTP fetch works and gets the GDP indicator back
            assertTrue(responseText.contains("NY.GDP.MKTP.CD") || responseText.contains("indicator"), "Response should contain GDP indicator string")
            assertTrue(responseText.length > 100, "Response should be a decently sized payload")
        } finally {
            client.close()
        }
    }
}
