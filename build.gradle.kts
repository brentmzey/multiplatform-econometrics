plugins {
    kotlin("jvm") version "2.0.0"
    application
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.github.ajalt.mordant:mordant:3.0.0")
    implementation("org.slf4j:slf4j-nop:2.0.13")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin:4.9.3")
}

application {
    mainClass.set("org.research.causal.EconometricsSuiteKt")
}

tasks.named<JavaExec>("run") {
    standardOutput = System.out
    errorOutput = System.err
}
