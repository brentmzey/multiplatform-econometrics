# Deep Dive: Instrumental Variables (IV) & Two-Stage Least Squares (2SLS)

To understand *why* we built this econometric suite, we must first understand the fundamental problem in causal inference: **Endogeneity**, and its mathematical cure: **Instrumental Variables**.

---

## 1. The Core Problem: Endogeneity
In a perfect world, we estimate the effect of $X$ (e.g., Education) on $Y$ (e.g., Wages) using Ordinary Least Squares (OLS):
$$ Y = \beta_0 + \beta_1 X + u $$
*(Where $u$ represents all unobserved factors affecting wages).*

For OLS to be unbiased, $X$ must be uncorrelated with the error term $u$. If they are correlated, we suffer from **Omitted Variable Bias (Endogeneity)**. 

**Example (The "Card" Dataset):**
People who choose to get more education ($X$) might also have higher innate "ability" or motivation. Because "ability" is unobservable, it falls into the error term $u$. Therefore, education ($X$) is correlated with ability ($u$).
If we run simple OLS, our estimate of $\beta_1$ is mathematically biased upwards because it absorbs the wage-boosting effect of ability. OLS is capturing *correlation*, not pure *causation*.

---

## 2. The Solution: Instrumental Variables (IV)
To find the true causal effect, we need a source of variation in $X$ that is completely independent of $u$. We need an **Instrument ($Z$)**.

A valid instrument must satisfy two strict assumptions:
1. **Relevance ($Cov(Z, X) \neq 0$)**: The instrument must actually affect the choice of $X$.
2. **Exclusion Restriction ($Cov(Z, u) = 0$)**: The instrument cannot affect $Y$ directly, *except* through its effect on $X$.

**Example:** David Card (1995) used "Proximity to a 4-year college" (`nearc4`) as the instrument ($Z$). 
*   *Relevance*: Living near a college lowers the cost of attending, so it increases years of education.
*   *Exclusion Restriction*: Growing up near a college doesn't inherently make you naturally "smarter" or directly boost your adult wages, except through the fact that it allowed you to get a degree.

---

## 3. The Mathematics of Two-Stage Least Squares (2SLS)
To execute the IV methodology, we use 2SLS. It mechanically scrubs the "dirty" endogenous variation out of $X$.

### Stage 1: Clean the Endogenous Variable
We regress the endogenous variable ($X$) on our instrument ($Z$) and all other exogenous control variables ($W$).
$$ X = \gamma_0 + \gamma_1 Z + \gamma_2 W + v $$

Once we run this regression, we use the estimated coefficients to calculate the **predicted values**, denoted as $\hat{X}$.
$\hat{X}$ represents *only* the variation in Education that is driven by college proximity. It is completely scrubbed clean of the unobservable "ability" bias!

### Stage 2: Estimate the Causal Effect
We replace the dirty $X$ with the clean $\hat{X}$ in our original wage equation:
$$ Y = \beta_0 + \beta_1 \hat{X} + \beta_2 W + \epsilon $$

When we run this second regression, the resulting $\beta_1$ is our true, unbiased **Causal Return to Education**.

---

## 4. How We Implemented This in Kotlin
Most data scientists rely on Python libraries (`linearmodels`) to do this automatically. But by understanding the math, we wrote the 2SLS logic natively in Kotlin using `Apache Commons Math`.

Here is the annotated logic from [EconometricsSuite.kt](file:///Users/brentzey/personal/econometric_causal_suite/econometric_causal_suite/src/main/kotlin/org/research/causal/EconometricsSuite.kt):

```kotlin
// Setup the Apache Math OLS solver
val stage1 = OLSMultipleLinearRegression()

// ---------------------------------------------------------
// STAGE 1: Regress Education (Endog) on Proximity (IV) + Controls
// ---------------------------------------------------------
val zData = observations.map { doubleArrayOf(it.exper, it.black, it.smsa, it.nearc4) }.toTypedArray()
stage1.newSampleData(educData, zData)
val s1Beta = stage1.estimateRegressionParameters()

// Mathematically compute X_hat (Fitted Education) for every person
val educHat = observations.mapIndexed { i, obs ->
    s1Beta[0] + // intercept
    s1Beta[1] * obs.exper + s1Beta[2] * obs.black + s1Beta[3] * obs.smsa + 
    s1Beta[4] * obs.nearc4 // The IV effect
}.toDoubleArray()

// ---------------------------------------------------------
// STAGE 2: Regress Wages (Y) on X_hat + Controls
// ---------------------------------------------------------
val stage2 = OLSMultipleLinearRegression()

// Swap the dirty 'educ' with the clean 'educHat'
val stage2X = observations.mapIndexed { i, obs ->
    doubleArrayOf(obs.exper, obs.black, obs.smsa, educHat[i])
}.toTypedArray()

stage2.newSampleData(yData, stage2X)

// The resulting coefficient for educHat is the Causal Estimator!
val ivBeta = stage2.estimateRegressionParameters() 
```

### The Results
When you run the suite, you see the mathematical proof of Omitted Variable Bias in action:
* **OLS Estimate**: `~0.076` (Naive: suggests 1 year of school = 7.6% wage boost).
* **IV 2SLS Estimate**: `~0.178` (Causal: actual causal impact is significantly higher when instrumented correctly). 

> [!TIP]
> In real-world econometrics, calculating the *Standard Errors* for Stage 2 requires a specific mathematical adjustment (because we are using predicted values $\hat{X}$ instead of actual $X$). Python's `linearmodels` handles this robust variance-covariance matrix adjustment automatically under the hood!
