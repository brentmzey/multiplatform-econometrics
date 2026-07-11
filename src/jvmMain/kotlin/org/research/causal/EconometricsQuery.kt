package org.research.causal

import com.expediagroup.graphql.server.operations.Query
import java.io.File
import kotlin.math.sqrt

class EconometricsQuery : Query {

    fun summaryStats(dataset: String, variables: List<String>): DatasetSummary {
        val file = File(dataset)
        if (!file.exists()) {
            throw IllegalArgumentException("Dataset $dataset not found")
        }
        val lines = file.readLines()
        if (lines.isEmpty()) throw IllegalArgumentException("Dataset empty")
        val header = lines.first().split(",").map { it.trim().removeSurrounding("\"") }
        
        val statsMap = variables.associateWith { DescriptiveStatistics() }
        val indices = variables.map { header.indexOf(it) }
        
        var count = 0
        for (line in lines.drop(1)) {
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            try {
                for (i in variables.indices) {
                    statsMap[variables[i]]?.addValue(parts[indices[i]].toDouble())
                }
                count++
            } catch (e: Exception) {}
        }
        
        val summaryList = variables.map {
            val stats = statsMap[it]!!
            SummaryStats(
                variable = it,
                mean = stats.mean,
                min = stats.min,
                max = stats.max,
                stdDev = stats.standardDeviation,
                count = stats.n.toInt()
            )
        }
        
        return DatasetSummary(dataset, count, summaryList)
    }

    fun runRegression(request: RegressionRequest): RegressionResult {
        val file = File(request.datasetUrlOrPath)
        if (!file.exists()) {
            throw IllegalArgumentException("Dataset not found")
        }
        val lines = file.readLines()
        val header = lines.first().split(",").map { it.trim().removeSurrounding("\"") }
        
        val yIdx = header.indexOf(request.yVar)
        val exogIdx = request.exogVars.map { header.indexOf(it) }
        val endogIdx = request.endogVar?.let { header.indexOf(it) }
        val ivIdx = request.ivVar?.let { header.indexOf(it) }
        
        val observations = mutableListOf<DoubleArray>()
        for (line in lines.drop(1)) {
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            try {
                val obs = mutableListOf(parts[yIdx].toDouble())
                exogIdx.forEach { obs.add(parts[it].toDouble()) }
                endogIdx?.let { obs.add(parts[it].toDouble()) }
                ivIdx?.let { obs.add(parts[it].toDouble()) }
                observations.add(obs.toDoubleArray())
            } catch (e: Exception) {}
        }

        val yData = observations.map { it[0] }.toDoubleArray()

        return if (request.endogVar != null && request.ivVar != null) {
            run2SLS(observations, yData, request)
        } else {
            runOLS(observations, yData, request)
        }
    }

    private fun runOLS(observations: List<DoubleArray>, yData: DoubleArray, request: RegressionRequest): RegressionResult {
        val olsX = observations.map { obs ->
            val x = mutableListOf(1.0) // Intercept
            for (i in request.exogVars.indices) x.add(obs[i + 1])
            x.toDoubleArray()
        }.toTypedArray()

        val ols = OLS(yData, olsX)
        val result = ols.estimate()

        val coeffs = mutableListOf<RegressionCoefficient>()
        coeffs.add(RegressionCoefficient("Intercept", result.beta[0], result.standardErrors[0], result.beta[0]/result.standardErrors[0]))
        request.exogVars.forEachIndexed { i, v ->
            val idx = i + 1
            coeffs.add(RegressionCoefficient(v, result.beta[idx], result.standardErrors[idx], result.beta[idx]/result.standardErrors[idx]))
        }

        return RegressionResult(
            estimator = "OLS",
            targetVariable = request.yVar,
            coefficients = coeffs,
            rSquared = result.rSquared,
            nobs = yData.size
        )
    }

    private fun run2SLS(observations: List<DoubleArray>, yData: DoubleArray, request: RegressionRequest): RegressionResult {
        val targetVarIdxInObs = 1 + request.exogVars.size
        val ivDataIdx = targetVarIdxInObs + 1
        val endogData = observations.map { it[targetVarIdxInObs] }.toDoubleArray()

        val zData = observations.map { obs ->
            val z = mutableListOf(1.0) // Intercept
            for (i in request.exogVars.indices) z.add(obs[i + 1])
            z.add(obs[ivDataIdx])
            z.toDoubleArray()
        }.toTypedArray()
        
        val stage1 = OLS(endogData, zData)
        val s1Result = stage1.estimate()
        val s1Beta = s1Result.beta

        val endogHat = observations.mapIndexed { i, obs ->
            var fitted = s1Beta[0]
            for (j in request.exogVars.indices) {
                fitted += s1Beta[j + 1] * obs[j + 1]
            }
            fitted += s1Beta.last() * obs[ivDataIdx]
            fitted
        }.toDoubleArray()

        val stage2X = observations.mapIndexed { i, obs ->
            val x = mutableListOf(1.0) // Intercept
            for (j in request.exogVars.indices) x.add(obs[j + 1])
            x.add(endogHat[i])
            x.toDoubleArray()
        }.toTypedArray()

        val stage2 = OLS(yData, stage2X)
        val result = stage2.estimate()

        val coeffs = mutableListOf<RegressionCoefficient>()
        coeffs.add(RegressionCoefficient("Intercept", result.beta[0], result.standardErrors[0], result.beta[0]/result.standardErrors[0]))
        request.exogVars.forEachIndexed { i, v ->
            val idx = i + 1
            coeffs.add(RegressionCoefficient(v, result.beta[idx], result.standardErrors[idx], result.beta[idx]/result.standardErrors[idx]))
        }
        coeffs.add(RegressionCoefficient(request.endogVar!!, result.beta.last(), result.standardErrors.last(), result.beta.last()/result.standardErrors.last()))

        return RegressionResult(
            estimator = "IV_2SLS",
            targetVariable = request.yVar,
            coefficients = coeffs,
            rSquared = result.rSquared,
            nobs = yData.size
        )
    }
}
