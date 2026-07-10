# Justfile for Econometrics Causal Suite
# Run `just` to see the list of available commands.

default:
    @just --list

# ==========================================
# 🐍 Python MVP Dashboard & Data
# ==========================================

# Install all Python dependencies using uv
setup-python:
    uv pip install -r requirements.txt || echo "Use specific uv commands based on your setup."
    uv run python -m pip install -U pip

# Generate datasets via Python
generate-data:
    uv run --with httpx --with fastapi --with jinja2 --with pandas --with linearmodels python generate_datasets.py

# Run the Python Dashboard locally on port 8000
run-dashboard:
    uv run --with fastapi --with httpx --with jinja2 --with python-multipart --with pandas --with pandas-datareader --with yfinance --with lxml --with seaborn --with matplotlib --with linearmodels uvicorn app:app --reload

# Run Python Tests
test-python:
    uv run --with pytest --with fastapi --with httpx --with jinja2 --with python-multipart --with pandas --with pandas-datareader --with yfinance --with lxml --with seaborn --with matplotlib --with linearmodels pytest test_app.py

# ==========================================
# ☕ Kotlin Multiplatform (KMP)
# ==========================================

# Run the Kotlin Backend Server natively
run-server:
    ./gradlew run

# Run the Kotlin Desktop Application (Compose for Desktop) natively
run-desktop:
    ./gradlew run -DmainClass=org.research.causal.DesktopAppKt

# ==========================================
# 🏗️ Build Targets
# ==========================================

# Build the JVM Desktop/Server Fat JAR
build-jvm:
    ./gradlew fatJar

# Build the Android APK
build-android:
    ./gradlew assembleDebug

# Build iOS Native Binaries & Frameworks
build-ios:
    ./gradlew iosX64Binaries iosArm64Binaries iosSimulatorArm64Binaries

# Build Web (JS & Wasm) Outputs
build-web:
    ./gradlew jsBrowserProductionWebpack wasmJsBrowserProductionWebpack

# Build ALL KMP Targets
build-all: build-jvm build-android build-ios build-web

# Run all shared Kotlin Tests
test-kmp:
    ./gradlew check

# Clean all Gradle build outputs
clean:
    ./gradlew clean
