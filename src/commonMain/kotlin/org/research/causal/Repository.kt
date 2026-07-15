package org.research.causal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.research.causal.db.DatabaseDriverFactory
import org.research.causal.db.PollingDatabase

class PollingRepository(
    private val client: PocketBaseClient,
    driverFactory: DatabaseDriverFactory
) {
    private val database: PollingDatabase? = try {
        driverFactory.createDriver()?.let { PollingDatabase(it) }
    } catch (e: Exception) {
        null
    }

    // In-memory fallback if SQLite is not available (e.g. WasmJS)
    private var inMemoryCache = emptyList<PollData>()

    suspend fun getPolls(forceRefresh: Boolean = false): List<PollData> {
        if (!forceRefresh) {
            val cached = getCachedPolls()
            if (cached.isNotEmpty()) return cached
        }

        return refreshFromNetwork()
    }

    private suspend fun getCachedPolls(): List<PollData> {
        if (database == null) return inMemoryCache

        return withContext(Dispatchers.Default) {
            val results = database.pollingDatabaseQueries.getAllResults().executeAsList()
            if (results.isEmpty()) return@withContext emptyList()

            val grouped = mutableMapOf<String, MutableList<CandidateResult>>()
            val pollInfo = mutableMapOf<String, Triple<String, String, String>>()

            results.forEach { row ->
                if (!grouped.containsKey(row.poll_id)) {
                    grouped[row.poll_id] = mutableListOf()
                    pollInfo[row.poll_id] = Triple(row.pollster, row.start_date, row.geo_name)
                }
                grouped[row.poll_id]?.add(CandidateResult(row.cand_name, row.party, row.pct))
            }

            pollInfo.map { (id, info) ->
                PollData(
                    id = id,
                    pollster = info.first,
                    startDate = info.second,
                    geography = info.third,
                    results = grouped[id]?.sortedByDescending { it.pct } ?: emptyList()
                )
            }.sortedByDescending { it.startDate }
        }
    }

    private suspend fun refreshFromNetwork(): List<PollData> {
        val response = client.getRecords(
            collectionName = "poll_results",
            expand = "poll_id,candidate_id,geography_id",
            sort = "-created",
            perPage = 300
        )

        val parsedPolls = mutableListOf<PollData>()
        val grouped = mutableMapOf<String, MutableList<CandidateResult>>()
        val pollInfo = mutableMapOf<String, Triple<String, String, String>>()

        database?.pollingDatabaseQueries?.transaction {
            // Optional: clear old data
            // database.pollingDatabaseQueries.deleteAllResults()
            // database.pollingDatabaseQueries.deleteAllPolls()

            response.items.forEach { item ->
                try {
                    val expand = item["expand"]?.jsonObject ?: return@forEach
                    val poll = expand["poll_id"]?.jsonObject ?: return@forEach
                    val cand = expand["candidate_id"]?.jsonObject ?: return@forEach
                    val geo = expand["geography_id"]?.jsonObject ?: return@forEach

                    val pollId = poll["id"]?.jsonPrimitive?.content ?: ""
                    val pollster = poll["pollster"]?.jsonPrimitive?.content ?: "Unknown"
                    val startDate = poll["start_date"]?.jsonPrimitive?.content?.take(10) ?: ""
                    val endDate = poll["end_date"]?.jsonPrimitive?.content?.take(10) ?: ""
                    val sampleSize = poll["sample_size"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
                    val pop = poll["population"]?.jsonPrimitive?.content ?: "rv"

                    val geoId = geo["id"]?.jsonPrimitive?.content ?: ""
                    val geoName = geo["name"]?.jsonPrimitive?.content ?: "US"
                    val geoLevel = geo["geo_level"]?.jsonPrimitive?.content ?: "state"

                    val candId = cand["id"]?.jsonPrimitive?.content ?: ""
                    val candName = cand["name"]?.jsonPrimitive?.content ?: "Unknown"
                    val party = cand["party"]?.jsonPrimitive?.content ?: "Unknown"
                    
                    val pct = item["pct"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                    val resultId = item["id"]?.jsonPrimitive?.content ?: ""
                    val created = item["created"]?.jsonPrimitive?.content ?: ""

                    // Save to SQLite
                    database.pollingDatabaseQueries.insertGeography(geoId, geoName, geoLevel)
                    database.pollingDatabaseQueries.insertCandidate(candId, candName, party)
                    database.pollingDatabaseQueries.insertPoll(pollId, pollster, startDate, endDate, sampleSize, pop)
                    database.pollingDatabaseQueries.insertPollResult(resultId, pollId, geoId, candId, pct, created)

                    // Build memory map for immediate return
                    if (!grouped.containsKey(pollId)) {
                        grouped[pollId] = mutableListOf()
                        pollInfo[pollId] = Triple(pollster, startDate, geoName)
                    }
                    grouped[pollId]?.add(CandidateResult(candName, party, pct))

                } catch (e: Exception) {
                    // Ignore malformed
                }
            }
        } ?: run {
            // No Database (e.g. WasmJS), just build in-memory map
            response.items.forEach { item ->
                try {
                    val expand = item["expand"]?.jsonObject ?: return@forEach
                    val poll = expand["poll_id"]?.jsonObject ?: return@forEach
                    val cand = expand["candidate_id"]?.jsonObject ?: return@forEach
                    val geo = expand["geography_id"]?.jsonObject ?: return@forEach

                    val pollId = poll["id"]?.jsonPrimitive?.content ?: ""
                    val pollster = poll["pollster"]?.jsonPrimitive?.content ?: "Unknown"
                    val startDate = poll["start_date"]?.jsonPrimitive?.content?.take(10) ?: ""
                    val geoName = geo["name"]?.jsonPrimitive?.content ?: "US"
                    val candName = cand["name"]?.jsonPrimitive?.content ?: "Unknown"
                    val party = cand["party"]?.jsonPrimitive?.content ?: "Unknown"
                    val pct = item["pct"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0

                    if (!grouped.containsKey(pollId)) {
                        grouped[pollId] = mutableListOf()
                        pollInfo[pollId] = Triple(pollster, startDate, geoName)
                    }
                    grouped[pollId]?.add(CandidateResult(candName, party, pct))
                } catch (e: Exception) { }
            }
        }

        val finalPolls = pollInfo.map { (id, info) ->
            PollData(
                id = id,
                pollster = info.first,
                startDate = info.second,
                geography = info.third,
                results = grouped[id]?.sortedByDescending { it.pct } ?: emptyList()
            )
        }.sortedByDescending { it.startDate }

        inMemoryCache = finalPolls
        return finalPolls
    }
}
