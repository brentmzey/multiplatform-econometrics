package org.research.causal

import kotlinx.serialization.Serializable

@Serializable
data class RegressionRequest(
    val datasetUrlOrPath: String,
    val yVar: String,
    val exogVars: List<String>,
    val endogVar: String? = null,
    val ivVar: String? = null
)

@Serializable
data class RegressionCoefficient(
    val variable: String,
    val estimate: Double,
    val stdError: Double,
    val tStat: Double
)

@Serializable
data class RegressionResult(
    val estimator: String,
    val targetVariable: String,
    val coefficients: List<RegressionCoefficient>,
    val rSquared: Double? = null,
    val nobs: Int
)

@Serializable
data class SummaryStats(
    val variable: String,
    val mean: Double,
    val min: Double,
    val max: Double,
    val stdDev: Double,
    val count: Int
)

@Serializable
data class DatasetSummary(
    val filename: String,
    val nobs: Int,
    val stats: List<SummaryStats>
)
