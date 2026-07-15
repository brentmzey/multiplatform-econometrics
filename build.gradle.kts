plugins {
    kotlin("multiplatform") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    kotlin("plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.6.11"
    id("com.android.application") version "8.2.0"
    id("app.cash.sqldelight") version "2.0.2"
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
    jvm {
        // Removed withJava() to avoid java plugin conflict
    }
    
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "21"
            }
        }
    }

/*
    wasmJs {
        moduleName = "econometricsApp"
        browser {
            commonWebpackConfig {
                outputFileName = "econometricsApp.js"
            }
        }
        binaries.executable()
    }
*/

    // Mobile targets for KMP
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation("io.ktor:ktor-client-core:3.0.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-mock:3.0.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("com.github.ajalt.mordant:mordant:3.0.0")
                implementation("org.jetbrains.lets-plot:lets-plot-kotlin:4.9.3")
                implementation("org.slf4j:slf4j-simple:2.0.13")
                
                // Ktor Server
                implementation("io.ktor:ktor-server-core-jvm:3.0.0")
                implementation("io.ktor:ktor-server-netty-jvm:3.0.0")
                implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.0")
                implementation("io.ktor:ktor-server-cors-jvm:3.0.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.0")
                implementation("io.ktor:ktor-client-cio:3.0.0")
                
                // GraphQL (Expedia Group)
                implementation("com.expediagroup:graphql-kotlin-ktor-server:7.1.1")

                // SQLDelight JVM
                implementation("app.cash.sqldelight:sqlite-driver:2.0.2")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("io.ktor:ktor-server-test-host:3.0.0")
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.activity:activity-compose:1.8.2")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("io.ktor:ktor-client-okhttp:3.0.0")
                
                // SQLDelight Android
                implementation("app.cash.sqldelight:android-driver:2.0.2")
            }
        }
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.0.0")
                // SQLDelight iOS
                implementation("app.cash.sqldelight:native-driver:2.0.2")
            }
        }
        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
        
/*
        val wasmJsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:3.0.0")
                // SQLDelight Wasm doesn't natively support SQLite easily out of box yet, we will fallback to in-memory here in the driver.
            }
        }
*/
    }
}

sqldelight {
    databases {
        create("PollingDatabase") {
            packageName.set("org.research.causal.db")
        }
    }
}

// Custom run task to replace application plugin
tasks.register<JavaExec>("run") {
    val mainClassProp = System.getProperty("mainClass") ?: "org.research.causal.ApplicationKt"
    mainClass.set(mainClassProp)
    val jvmMain = kotlin.targets.getByName("jvm").compilations.getByName("main")
    classpath = jvmMain.output.allOutputs + (jvmMain.runtimeDependencyFiles ?: files())
    standardOutput = System.out
    errorOutput = System.err
}

// Custom fatJar task replacing application plugin's built-in fat jar behavior
tasks.register<Jar>("fatJar") {
    val jvmMain = kotlin.targets.getByName("jvm").compilations.getByName("main")
    val dependencies = (jvmMain.runtimeDependencyFiles ?: files()).map { if (it.isDirectory) it else zipTree(it) }
    from(jvmMain.output.allOutputs)
    from(dependencies)
    archiveClassifier.set("fat")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "org.research.causal.ApplicationKt"
    }
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
