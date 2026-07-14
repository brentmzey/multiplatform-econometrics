## [1.6.1](https://github.com/brentmzey/multiplatform-econometrics/compare/v1.6.0...v1.6.1) (2026-07-14)


### Bug Fixes

* **db:** Update schema migration for PocketBase v0.22+ to use fields instead of schema ([e7cef2a](https://github.com/brentmzey/multiplatform-econometrics/commit/e7cef2a898dba2af17bb2e398064627b6eb2082e))
* **db:** Update schema migration for PocketBase v0.22+ to use fields instead of schema ([c087319](https://github.com/brentmzey/multiplatform-econometrics/commit/c0873193f534a1e6e345f25d7cae0238c0c64fce))

# [1.6.0](https://github.com/brentmzey/multiplatform-econometrics/compare/v1.5.0...v1.6.0) (2026-07-14)


### Bug Fixes

* **data:** Update scraper to read local CSV to bypass Cloudflare bot protection ([0a072d4](https://github.com/brentmzey/multiplatform-econometrics/commit/0a072d4f4da3a8e544ac73bc1ded6dcbc87c6ed4))
* **data:** Update scraper to use geo_level instead of level ([d1b100c](https://github.com/brentmzey/multiplatform-econometrics/commit/d1b100c0be63fbebb5251ea37d1330ced2aa5001))
* **db:** Remove custom indices from migration script for PocketBase 0.22 compatibility ([88781e6](https://github.com/brentmzey/multiplatform-econometrics/commit/88781e6646e462b0e33911fd15f3ea0c95da241c))


### Features

* **data:** Add FiveThirtyEight polling data scraper and normalized PocketHost ingestion script ([a1ba574](https://github.com/brentmzey/multiplatform-econometrics/commit/a1ba574b8535fc905fbf4297526a3f67bf6d81c8))
* **db:** Add normalized polling schema with xref relations and DB indexing ([a9aa00f](https://github.com/brentmzey/multiplatform-econometrics/commit/a9aa00f684b5afd8d2bd1911d137b2b6ca8cdc0b))

# [1.5.0](https://github.com/brentmzey/multiplatform-econometrics/compare/v1.4.0...v1.5.0) (2026-07-11)


### Features

* Add CSV upload functionality and dynamic dataset selector to Dashboard ([3000a41](https://github.com/brentmzey/multiplatform-econometrics/commit/3000a4100faffdffe01a1cd2b8cdf5fe6c051ecc))
* Add CSV upload functionality and dynamic dataset selector to Dashboard ([abb9fd9](https://github.com/brentmzey/multiplatform-econometrics/commit/abb9fd981ae1ae9e00bb0c51fb1bb2d7390f9918))

# [1.4.0](https://github.com/brentmzey/multiplatform-econometrics/compare/v1.3.0...v1.4.0) (2026-07-11)


### Features

* Wire up PocketHost URL and add TypeScript seed migration ([3771757](https://github.com/brentmzey/multiplatform-econometrics/commit/37717573dfd5c8c78d9c79a2a3c3c6f01afa1e9b))
* Wire up PocketHost URL and add TypeScript seed migration ([b23f9f7](https://github.com/brentmzey/multiplatform-econometrics/commit/b23f9f705157be62318455f53e9dc6587d42ceac))

# [1.3.0](https://github.com/brentmzey/multiplatform-econometrics/compare/v1.2.0...v1.3.0) (2026-07-11)


### Features

* Add dynamic dataset parser, variable selection UI, and dynamic OLS execution to Dashboard ([8591370](https://github.com/brentmzey/multiplatform-econometrics/commit/859137021442d8af8c5ab2a49381f83edcdb5e2b))
* Add dynamic dataset parser, variable selection UI, and dynamic OLS execution to Dashboard ([082e263](https://github.com/brentmzey/multiplatform-econometrics/commit/082e2634338b3026694acd3c43cb315d26fc9c58))

# [1.2.0](https://github.com/brentmzey/multiplatform-econometrics/compare/v1.1.0...v1.2.0) (2026-07-11)


### Features

* Add Compose UI with PocketBase Login and Math Dashboard, plus robust Math unit tests ([bcdf246](https://github.com/brentmzey/multiplatform-econometrics/commit/bcdf24678534d364a16efd86b73425d736341e25))

# [1.1.0](https://github.com/brentmzey/multiplatform-econometrics/compare/v1.0.0...v1.1.0) (2026-07-11)


### Features

* Add PocketBase Ktor client for authentication and federated data ([24c1f5d](https://github.com/brentmzey/multiplatform-econometrics/commit/24c1f5d8e38129acb8bcb0ded06875ad475dac6f))
* Wire Compose UI to shared API client to fetch live data ([96f9caa](https://github.com/brentmzey/multiplatform-econometrics/commit/96f9caa02979b55158b1c7c234efbcfe3f3e8b0f))

# 1.0.0 (2026-07-10)


### Bug Fixes

* Add explicit Ktor client engines for Android and iOS targets ([75175ee](https://github.com/brentmzey/multiplatform-econometrics/commit/75175ee76718a1f063a2505c9f080c081b1bb0a7))
* bump ktor client dependencies to 3.0.0 to enable wasmJs compatibility ([7709f07](https://github.com/brentmzey/multiplatform-econometrics/commit/7709f07ccda41d31af485398ccedde3bd87b28aa))
* enable AndroidX support to resolve KMP dependency crash ([21fc16e](https://github.com/brentmzey/multiplatform-econometrics/commit/21fc16eb3198c1de38e576be027ed2dcd39226b2))
* Move WorldBankIntegrationTest to jvmTest to avoid Wasm CORS and iOS ATS sandbox restrictions ([9ea342b](https://github.com/brentmzey/multiplatform-econometrics/commit/9ea342b84094dc6ebe61bfaecfa21a92d95c99e4))
* remove application plugin to resolve Android plugin conflict and update run tasks ([8cc9a59](https://github.com/brentmzey/multiplatform-econometrics/commit/8cc9a594eaa38b5429ceae718af36a9325b58ad7))
* resolve fatJar task name clash and classpath nullability ([747ecd8](https://github.com/brentmzey/multiplatform-econometrics/commit/747ecd8bf32b0926059cc265979b5ef6df1453e0))


### Features

* add Compose Multiplatform Android, Web, iOS shared UI and Github Releases ([c404c30](https://github.com/brentmzey/multiplatform-econometrics/commit/c404c30cb343375fae906d964b2ae5c9528251ac))
* add Justfile for semantic local development command chaining ([110d126](https://github.com/brentmzey/multiplatform-econometrics/commit/110d126f7bd1f44d90a5a0ea1673b8fa0228bfc2))
