package org.research.causal

object SampleData {
    // A tiny sample of Return to Education (Card) dataset for demo purposes
    // Columns: lwage, educ, exper, black, smsa, nearc4
    val cardCsv = """
lwage,educ,exper,black,smsa,nearc4
5.5,12,16,1,1,1
6.1,16,9,0,1,1
5.8,14,12,0,1,1
6.5,18,5,0,1,1
5.2,10,20,1,0,0
5.9,16,8,0,0,1
6.2,14,14,0,1,0
5.6,12,15,1,1,1
6.0,16,10,0,1,1
5.4,12,18,1,0,0
    """.trimIndent()
}

data class ParsedDataset(
    val name: String,
    val headers: List<String>,
    val rows: List<List<Double>>
)

fun parseCsv(name: String, csvString: String): ParsedDataset {
    val lines = csvString.trim().split("\n")
    if (lines.isEmpty()) return ParsedDataset(name, emptyList(), emptyList())
    
    val headers = lines.first().split(",").map { it.trim() }
    val rows = lines.drop(1).mapNotNull { line ->
        try {
            line.split(",").map { it.trim().toDouble() }
        } catch (e: Exception) {
            null
        }
    }
    
    return ParsedDataset(name, headers, rows)
}
