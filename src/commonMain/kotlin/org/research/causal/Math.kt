package org.research.causal

import kotlin.math.sqrt

class Matrix(val data: Array<DoubleArray>) {
    val rows: Int = data.size
    val cols: Int = if (rows > 0) data[0].size else 0

    operator fun get(i: Int, j: Int): Double = data[i][j]
    operator fun set(i: Int, j: Int, value: Double) { data[i][j] = value }

    fun transpose(): Matrix {
        val transposed = Array(cols) { DoubleArray(rows) }
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                transposed[j][i] = data[i][j]
            }
        }
        return Matrix(transposed)
    }

    operator fun times(other: Matrix): Matrix {
        require(cols == other.rows) { "Matrix dimensions do not match for multiplication: $cols != ${other.rows}" }
        val result = Array(rows) { DoubleArray(other.cols) }
        for (i in 0 until rows) {
            for (j in 0 until other.cols) {
                var sum = 0.0
                for (k in 0 until cols) {
                    sum += data[i][k] * other.data[k][j]
                }
                result[i][j] = sum
            }
        }
        return Matrix(result)
    }

    fun invert(): Matrix {
        require(rows == cols) { "Matrix must be square to invert" }
        val n = rows
        val augmented = Array(n) { DoubleArray(2 * n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                augmented[i][j] = data[i][j]
            }
            augmented[i][n + i] = 1.0
        }

        // Gauss-Jordan Elimination
        for (i in 0 until n) {
            // Find pivot
            var maxRow = i
            for (k in i + 1 until n) {
                if (kotlin.math.abs(augmented[k][i]) > kotlin.math.abs(augmented[maxRow][i])) {
                    maxRow = k
                }
            }
            
            // Swap rows
            val temp = augmented[i]
            augmented[i] = augmented[maxRow]
            augmented[maxRow] = temp

            val pivot = augmented[i][i]
            require(kotlin.math.abs(pivot) > 1e-12) { "Matrix is singular or nearly singular" }

            // Scale pivot row
            for (j in 0 until 2 * n) {
                augmented[i][j] /= pivot
            }

            // Eliminate other rows
            for (k in 0 until n) {
                if (k != i) {
                    val factor = augmented[k][i]
                    for (j in 0 until 2 * n) {
                        augmented[k][j] -= factor * augmented[i][j]
                    }
                }
            }
        }

        val inverse = Array(n) { DoubleArray(n) }
        for (i in 0 until n) {
            for (j in 0 until n) {
                inverse[i][j] = augmented[i][n + j]
            }
        }
        return Matrix(inverse)
    }
}

class OLSResult(
    val beta: DoubleArray,
    val standardErrors: DoubleArray,
    val rSquared: Double,
    val residuals: DoubleArray
)

class OLS(val y: DoubleArray, val x: Array<DoubleArray>) {
    // x should already include a column of 1s if an intercept is desired
    val n = y.size
    val k = if (x.isNotEmpty()) x[0].size else 0

    fun estimate(): OLSResult {
        val yMat = Matrix(Array(n) { i -> DoubleArray(1) { y[i] } })
        val xMat = Matrix(x)
        val xT = xMat.transpose()

        // beta = (X^T X)^-1 X^T Y
        val xTx = xT * xMat
        val xTxInv = xTx.invert()
        val betaMat = (xTxInv * xT) * yMat
        val beta = DoubleArray(k) { i -> betaMat[i, 0] }

        // Residuals & RSS
        val residuals = DoubleArray(n)
        var rss = 0.0
        for (i in 0 until n) {
            var yHat = 0.0
            for (j in 0 until k) {
                yHat += beta[j] * x[i][j]
            }
            residuals[i] = y[i] - yHat
            rss += residuals[i] * residuals[i]
        }

        // Standard Errors
        val sigma2 = rss / (n - k)
        val se = DoubleArray(k)
        for (j in 0 until k) {
            se[j] = sqrt(sigma2 * xTxInv[j, j])
        }

        // R-squared
        val yMean = y.average()
        var tss = 0.0
        for (i in 0 until n) {
            val diff = y[i] - yMean
            tss += diff * diff
        }
        val rSquared = if (tss > 0.0) 1.0 - (rss / tss) else 1.0

        return OLSResult(beta, se, rSquared, residuals)
    }
}

class DescriptiveStatistics {
    private var sum = 0.0
    private var sumSq = 0.0
    var n = 0.0
    var min = Double.MAX_VALUE
    var max = -Double.MAX_VALUE

    fun addValue(v: Double) {
        sum += v
        sumSq += v * v
        if (v < min) min = v
        if (v > max) max = v
        n++
    }

    val mean: Double get() = if (n > 0) sum / n else Double.NaN
    
    val standardDeviation: Double get() {
        if (n < 2) return Double.NaN
        val variance = (sumSq - (sum * sum) / n) / (n - 1)
        return kotlin.math.sqrt(variance)
    }
}
