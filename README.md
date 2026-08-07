# Florence Mobile (Android)

Florence'ın native Android uygulaması — BIST hisseleri, döviz kurları, kıymetli metaller, yapay zekâ destekli raporlar ve sanal portföy yönetimi.

Bu depo, [project-florence/web](https://github.com/project-florence/web) arayüzünün ve [project-florence/backend](https://github.com/project-florence/backend) API sunucusunun mobil karşılığıdır. API sözleşmeleri backend'deki `/api/v1` uçlarıyla birebir eşleşir.

## Teknoloji

- Kotlin 2.x, Jetpack Compose (Material 3), Navigation Compose
- Hilt (DI), Retrofit + OkHttp + kotlinx.serialization
- EncryptedSharedPreferences (refresh token), MVVM + Repository
- minSdk 26 / targetSdk 36

## Build

```bash
# Dev (yerel backend: http://10.0.2.2:7055)
./gradlew :app:assembleDevDebug

# Prod
./gradlew :app:assembleProdDebug
```

Not: Sistemde Java 8 kurulu olabilir; Gradle, `gradle.properties` içindeki `org.gradle.java.home` ile Android Studio'nun JBR (JDK 21) sürümünü kullanır. `GRADLE_USER_HOME` `C:/gradle-home` olarak ayarlıdır (gradlew içinde) — Windows kullanıcı adındaki kesme işareti (`GAMER'S`) Java'nın @argfile ayrıştırmasını bozduğu için Gradle home ve proje apostrophesiz yollarda tutulur.

## Emülatörde yerel backend

Backend `docker-compose up` ile `:7055` portunda çalışıyorsa, emülatör `10.0.2.2:7055` üzerinden erişir. Alternatif olarak:

```bash
adb reverse tcp:7055 tcp:7055
```

ile `localhost:7055` kullanılabilir. Yalnızca dev hostlarına cleartext HTTP izni verilmiştir (`res/xml/network_security_config.xml`).

## Kimlik Doğrulama (backend uyumluluğu)

- Login: `POST /api/v1/auth/login` — **form-encoded** (OAuth2PasswordRequestForm), yanıt JSON token çifti
- Access token 1 saat geçerli; `TokenRefreshAuthenticator` 401'de tek seferlik (single-flight) refresh yapar ve isteği yeniden dener
- Refresh: `POST /api/v1/auth/refresh` (JSON `{ refresh_token }`) — backend rate limiti 5 istek/dk
- Refresh token EncryptedSharedPreferences'ta, access token bellekte tutulur

## Mimari

```
app/src/main/java/com/florence/app/
├── core/          # ağ (interceptor/authenticator), token deposu, tema, DI
├── data/          # Retrofit API arayüzü, DTO'lar, repository'ler
└── presentation/  # Compose ekranlar, ViewModel'ler, navigasyon
```

API katmanı saf Kotlin + kotlinx.serialization ile ayrıştırılmıştır; ileride iOS hedeflenirse KMP modülüne taşınabilir.

## Lisans

AGPL-3.0 — ayrıntılar için [LICENSE](LICENSE).
