# Florence Mobile Agent Guide

## Setup and Build

- JDK: Android Studio JBR (21) — `gradle.properties` → `org.gradle.java.home=C:/Program Files/Android/Android Studio1/jbr`. System Java 8 is NOT sufficient for AGP 8.x.
- SDK: `local.properties` → `sdk.dir` (ANDROID_HOME). `compileSdk = 36`.
- Build variants: `dev` (emulator → `http://10.0.2.2:7055/`, appId suffix `.dev`) and `prod` (`https://api.florencex.com.tr/`).
- Build: `./gradlew :app:assembleDevDebug` (dev) or `:app:assembleProdDebug`.
- Unit tests: `./gradlew :app:testDevDebugUnitTest`.

## Structure

- `core/` — networking (AuthInterceptor, TokenRefreshAuthenticator), token storage (EncryptedSharedPreferences), Material3 themes, Hilt modules.
- `data/` — Retrofit interfaces (`FlorenceApi`, `AuthEndpoints`), kotlinx-serialization DTOs, repositories.
- `presentation/` — Compose screens, ViewModels (StateFlow), navigation (FlorenceRoot gates on session).

## Backend Contract (must stay in sync with project-florence/backend)

- All endpoints under `/api/v1` (FastAPI). Production disables `/openapi.json` — regenerate DTOs from a dev instance.
- Login is form-encoded (`OAuth2PasswordRequestForm`), NOT JSON.
- Access token expires in 1 hour (HS256); refresh token persists in DB (days).
- Refresh endpoint is rate-limited to 5/min (Redis) — never loop refreshes.
- Market endpoints are guest-accessible; auth endpoints and portfolios/reports require tokens.
- CORS does not apply to native OkHttp clients.

## Verification

- After Kotlin changes: `./gradlew :app:compileDevDebugKotlin` for a fast compile check.
- Full check before commit: `./gradlew :app:testDevDebugUnitTest :app:lintDevDebug`.
- Manual E2E: run backend on `:7055` (docker compose), start emulator, install `app-dev-debug.apk`, register → dashboard shows live currencies/tickers.
