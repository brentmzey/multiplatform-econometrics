package org.research.causal

import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.geom.geomSmooth
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.label.labs
import org.jetbrains.letsPlot.pos.positionJitter
import org.jetbrains.letsPlot.intern.Plot
import java.io.File

fun main() {
    println("Generating Data.gov Nutrition/Obesity Visualizations in Kotlin...")
    
    // We'll read the CSV downloaded from data.gov and create a simple chart
    val file: File = File("nutrition_obesity.csv")
    if (!file.exists()) {
        println("nutrition_obesity.csv not found. Run python downloader first.")
        return
    }

    val lines: List<String> = file.readLines()
    val header: List<String> = lines.first().split(",").map { it.trim().removeSurrounding("\"") }
    val valIdx: Int = header.indexOf("data_value")
    val classIdx: Int = header.indexOf("class")

    val classes: MutableList<String> = mutableListOf<String>()
    val values: MutableList<Double> = mutableListOf<Double>()

    for (line: String in lines.drop(1)) {
        val parts: List<String> = line.split(",").map { it.trim().removeSurrounding("\"") }
        try {
            val v: Double = parts[valIdx].toDouble()
            val c: String = parts[classIdx]
            classes.add(c)
            values.add(v)
        } catch (e: Exception) {}
    }

    val data: Map<String, List<Any>> = mapOf(
        "Class" to classes,
        "Data_Value" to values
    )

    val p = letsPlot(data) { x = "Class"; y = "Data_Value" } + 
            geomPoint(color = "#3b82f6", alpha = 0.5, position = positionJitter()) + 
            labs(
                title = "Nutrition, Physical Activity, and Obesity (Data.gov)", 
                subtitle = "Values by Class",
                x = "Class Category", 
                y = "Data Value (%)"
            )

    ggsave(p, "nutrition_obesity_kotlin.html")
    println("Saved nutrition_obesity_kotlin.html")

    // Synthetic econometric regression chart
    val rand: java.util.Random = java.util.Random(42)
    val n: Int = 200
    val xData: List<Double> = List(n) { rand.nextGaussian() * 10 + 50 }
    val yData: List<Double> = xData.map { it * 0.8 + rand.nextGaussian() * 5 + 10 }
    
    val synthData: Map<String, List<Double>> = mapOf("HealthIndex" to xData, "Outcome" to yData)
    val p2 = letsPlot(synthData) { x = "HealthIndex"; y = "Outcome" } +
             geomPoint(color = "#10b981", alpha = 0.6) +
             geomSmooth(method = "lm", color = "#ef4444", size = 2) +
             labs(title = "Econometric Correlation: Health Index vs Outcome", x = "Health Index", y = "Outcome")
             
    ggsave(p2, "econometric_regression_kotlin.html")
    println("Saved econometric_regression_kotlin.html")
}
