package org.research.causal

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.*
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import java.io.File

data class DatasetConfig(
    val name: String,
    val file: String,
    val yVar: String,
    val exogVars: List<String>,
    val endogVar: String? = null,
    val ivVar: String? = null
)

fun main() {
    val t = Terminal()
    
    t.println(cyan(bold("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓")))
    t.println(cyan(bold("┃ Kotlin Econometric Engine                                    ┃")))
    t.println(cyan("┃ ") + dim("Stack: Java 21 LTS | Kotlin Data Classes | Commons Math   ") + cyan("┃"))
    t.println(cyan(bold("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛")))

    val datasets = listOf(
        DatasetConfig("Return to Education (Card)", "card.csv", "lwage", listOf("exper", "black", "smsa"), "educ", "nearc4"),
        DatasetConfig("Female Wage (Mroz)", "mroz.csv", "wage", listOf("educ", "exper", "age", "kidslt6")),
        DatasetConfig("Men's Wage (Wage)", "wage.csv", "lwage", listOf("educ", "exper", "tenure", "black")),
        DatasetConfig("Birthweight (BW)", "birthweight.csv", "bwght", listOf("cigs", "faminc", "parity", "male"))
    )

    for (config in datasets) {
        val file = File(config.file)
        if (!file.exists()) {
            t.println(yellow("Skipping ${config.name}, ${config.file} not found."))
            continue
        }

        t.print(green("ℹ ") + "Reading ${config.file} from disk... ")
        val lines = file.readLines()
        if (lines.isEmpty()) continue

        val header = lines.first().split(",").map { it.trim().removeSurrounding("\"") }
        val yIdx = header.indexOf(config.yVar)
        val exogIdx = config.exogVars.map { header.indexOf(it) }
        val endogIdx = config.endogVar?.let { header.indexOf(it) }
        val ivIdx = config.ivVar?.let { header.indexOf(it) }

        val observations = mutableListOf<DoubleArray>()
        for (line in lines.drop(1)) {
            val parts = line.split(",").map { it.trim().removeSurrounding("\"") }
            try {
                val y = parts[yIdx].toDouble()
                val exogs = exogIdx.map { parts[it].toDouble() }
                val endog = endogIdx?.let { parts[it].toDouble() }
                val iv = ivIdx?.let { parts[it].toDouble() }
                
                val obs = mutableListOf(y)
                obs.addAll(exogs)
                if (endog != null) obs.add(endog)
                if (iv != null) obs.add(iv)
                
                observations.add(obs.toDoubleArray())
            } catch (e: Exception) {
                // Skip invalid rows
            }
        }

        val n = observations.size
        t.println(bold(green("DONE ")) + dim("($n observations)"))
        t.println()

        val yData = observations.map { it[0] }.toDoubleArray()

        t.println(table {
            header {
                row {
                    cell(bold("Estimator"))
                    cell(bold("Variable"))
                    cell(bold("Estimate (β)"))
                    cell(bold("Std. Error"))
                    cell(bold("t-Statistic"))
                }
            }
            body {
                val targetVar = config.endogVar ?: config.exogVars[0]
                val targetVarIdxInObs = if (config.endogVar != null) 1 + config.exogVars.size else 1
                val targetBetaIdx = if (config.endogVar != null) config.exogVars.size + 1 else 1

                // 1. OLS
                val ols = OLSMultipleLinearRegression()
                val olsX = observations.map { obs ->
                    val x = mutableListOf<Double>()
                    for (i in config.exogVars.indices) x.add(obs[i + 1])
                    if (config.endogVar != null) x.add(obs[targetVarIdxInObs])
                    x.toDoubleArray()
                }.toTypedArray()
                
                ols.newSampleData(yData, olsX)
                val olsBeta = ols.estimateRegressionParameters()
                val olsSe = ols.estimateRegressionParametersStandardErrors()
                val olsT = olsBeta[targetBetaIdx] / olsSe[targetBetaIdx]

                row {
                    cell("OLS")
                    cell(targetVar)
                    cell(String.format("%.4f", olsBeta[targetBetaIdx]))
                    cell(String.format("%.4f", olsSe[targetBetaIdx]))
                    cell(String.format("%.2f", olsT))
                }

                // 2. IV 2SLS (if applicable)
                if (config.endogVar != null && config.ivVar != null && endogIdx != null && ivIdx != null) {
                    val endogData = observations.map { it[targetVarIdxInObs] }.toDoubleArray()
                    val ivDataIdx = 1 + config.exogVars.size + 1

                    // Stage 1: Regress endogenous on instrument + exogenous
                    val stage1 = OLSMultipleLinearRegression()
                    val zData = observations.map { obs ->
                        val z = mutableListOf<Double>()
                        for (i in config.exogVars.indices) z.add(obs[i + 1])
                        z.add(obs[ivDataIdx]) // iv is last
                        z.toDoubleArray()
                    }.toTypedArray()
                    
                    stage1.newSampleData(endogData, zData)
                    val s1Beta = stage1.estimateRegressionParameters()

                    // Predict fitted values
                    val endogHat = observations.mapIndexed { i, obs ->
                        var fitted = s1Beta[0]
                        for (j in config.exogVars.indices) {
                            fitted += s1Beta[j + 1] * obs[j + 1]
                        }
                        fitted += s1Beta[s1Beta.size - 1] * obs[ivDataIdx]
                        fitted
                    }.toDoubleArray()

                    // Stage 2: Regress Y on predicted endogenous + exogenous
                    val stage2 = OLSMultipleLinearRegression()
                    val stage2X = observations.mapIndexed { i, obs ->
                        val x = mutableListOf<Double>()
                        for (j in config.exogVars.indices) x.add(obs[j + 1])
                        x.add(endogHat[i])
                        x.toDoubleArray()
                    }.toTypedArray()

                    stage2.newSampleData(yData, stage2X)
                    val ivBeta = stage2.estimateRegressionParameters()
                    val ivSe = stage2.estimateRegressionParametersStandardErrors()
                    val targetIvIdx = stage2X[0].size // Since endogHat is added last, its param index is last
                    val ivT = ivBeta[targetIvIdx] / ivSe[targetIvIdx]

                    row {
                        cell(green(bold("IV 2SLS")))
                        cell(targetVar)
                        cell(green(bold(String.format("%.4f", ivBeta[targetIvIdx]))))
                        cell(String.format("%.4f", ivSe[targetIvIdx]))
                        cell(String.format("%.2f", ivT))
                    }
                }
            }
        })
        t.println()
    }
    t.println(dim("✔ Kotlin JVM execution completed successfully."))
}
