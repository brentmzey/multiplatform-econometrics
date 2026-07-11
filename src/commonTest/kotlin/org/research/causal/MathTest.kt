package org.research.causal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathTest {

    @Test
    fun testDescriptiveStatistics() {
        val stats = DescriptiveStatistics()
        val data = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        data.forEach { stats.addValue(it) }

        assertEquals(5.0, stats.n)
        assertEquals(1.0, stats.min)
        assertEquals(5.0, stats.max)
        assertEquals(3.0, stats.mean)
        
        // Variance of 1,2,3,4,5 = 2.5. StdDev = sqrt(2.5) ~ 1.5811
        assertTrue(kotlin.math.abs(stats.standardDeviation - 1.5811) < 0.001)
    }

    @Test
    fun testMatrixMultiplication() {
        // 2x3 matrix
        val m1 = Matrix(arrayOf(
            doubleArrayOf(1.0, 2.0, 3.0),
            doubleArrayOf(4.0, 5.0, 6.0)
        ))
        
        // 3x2 matrix
        val m2 = Matrix(arrayOf(
            doubleArrayOf(7.0, 8.0),
            doubleArrayOf(9.0, 1.0),
            doubleArrayOf(2.0, 3.0)
        ))
        
        val result = m1 * m2
        
        assertEquals(2, result.rows)
        assertEquals(2, result.cols)
        
        // 1*7 + 2*9 + 3*2 = 7 + 18 + 6 = 31
        assertEquals(31.0, result[0, 0])
        // 1*8 + 2*1 + 3*3 = 8 + 2 + 9 = 19
        assertEquals(19.0, result[0, 1])
        // 4*7 + 5*9 + 6*2 = 28 + 45 + 12 = 85
        assertEquals(85.0, result[1, 0])
        // 4*8 + 5*1 + 6*3 = 32 + 5 + 18 = 55
        assertEquals(55.0, result[1, 1])
    }
    
    @Test
    fun testMatrixInversion() {
        val m = Matrix(arrayOf(
            doubleArrayOf(4.0, 7.0),
            doubleArrayOf(2.0, 6.0)
        ))
        // det = 24 - 14 = 10
        // inv = [0.6, -0.7; -0.2, 0.4]
        
        val inv = m.invert()
        assertTrue(kotlin.math.abs(inv[0, 0] - 0.6) < 1e-9)
        assertTrue(kotlin.math.abs(inv[0, 1] - (-0.7)) < 1e-9)
        assertTrue(kotlin.math.abs(inv[1, 0] - (-0.2)) < 1e-9)
        assertTrue(kotlin.math.abs(inv[1, 1] - 0.4) < 1e-9)
    }

    @Test
    fun testOLSRegression() {
        val y = doubleArrayOf(2.0, 4.0, 5.0, 4.0, 5.0)
        // X with intercept
        val x = arrayOf(
            doubleArrayOf(1.0, 1.0),
            doubleArrayOf(1.0, 2.0),
            doubleArrayOf(1.0, 3.0),
            doubleArrayOf(1.0, 4.0),
            doubleArrayOf(1.0, 5.0)
        )
        
        val ols = OLS(y, x)
        val result = ols.estimate()
        
        // We expect positive beta for x1
        assertEquals(2, result.beta.size)
        assertTrue(result.beta[1] > 0)
        assertTrue(result.rSquared > 0.0)
        assertEquals(5, result.residuals.size)
    }
}
