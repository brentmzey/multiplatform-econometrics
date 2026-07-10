package org.research.causal

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.io.File

class EconometricsQueryTest {
    @Test
    fun testSummaryStats() {
        val testFile = File("test_data.csv")
        testFile.writeText("""
            "id","income","education"
            "1","50000","12"
            "2","60000","14"
            "3","55000","13"
        """.trimIndent())
        
        try {
            val query = EconometricsQuery()
            val result = query.summaryStats("test_data.csv", listOf("income", "education"))
            
            assertEquals("test_data.csv", result.filename)
            assertEquals(3, result.nobs)
            assertEquals(2, result.stats.size)
            
            val incomeStat = result.stats.find { it.variable == "income" }
            assertNotNull(incomeStat)
            assertEquals(55000.0, incomeStat.mean)
            
        } finally {
            testFile.delete()
        }
    }
}
