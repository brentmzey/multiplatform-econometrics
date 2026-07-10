plugins {
    kotlin("multiplatform") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    kotlin("plugin.compose") version "2.0.0"
    id("org.jetbrains.compose") version "1.6.11"
    id("com.android.application") version "8.2.0"
    application
    id("io.ktor.plugin") version "2.3.11"
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    jvm {
        withJava()
    }
    
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
        }
    }

    wasmJs {
        moduleName = "econometricsApp"
        browser {
            commonWebpackConfig {
                outputFileName = "econometricsApp.js"
            }
        }
        binaries.executable()
    }

    // Mobile targets for KMP
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.apache.commons:commons-math3:3.6.1")
                implementation("com.github.ajalt.mordant:mordant:3.0.0")
                implementation("org.jetbrains.lets-plot:lets-plot-kotlin:4.9.3")
                implementation("org.slf4j:slf4j-simple:2.0.13")
                
                // Ktor Server
                implementation("io.ktor:ktor-server-core-jvm:2.3.11")
                implementation("io.ktor:ktor-server-netty-jvm:2.3.11")
                implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.11")
                implementation("io.ktor:ktor-server-cors-jvm:2.3.11")
                implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.11")
                
                // GraphQL (Expedia Group)
                implementation("com.expediagroup:graphql-kotlin-ktor-server:7.1.1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("io.ktor:ktor-server-test-host:2.3.11")
            }
        }
    }
}



application {
    mainClass.set("org.research.causal.ApplicationKt")
}

tasks.named<JavaExec>("run") {
    standardOutput = System.out
    errorOutput = System.err
}

android {
    namespace = "org.research.causal"
    compileSdk = 34
    defaultConfig {
        applicationId = "org.research.causal"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
