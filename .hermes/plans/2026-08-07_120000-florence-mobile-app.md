# Florence Mobil Uygulama (Android) — İmplementasyon Planı

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Florence'ın mevcut web uygulaması (project-florence/web) ve API sunucusuyla (project-florence/backend) birebir uyumlu, native bir Android mobil uygulaması geliştirmek.

**Architecture:** Tek modül, MVVM + katmanlı yapı — `presentation` (Compose UI + ViewModel/StateFlow) / `domain` (use case'ler) / `data` (Retrofit + OkHttp + Room cache + repository). API/veri katmanı saf Kotlin + kotlinx.serialization ile ayrıştırılır ki ileride KMP modülüne taşınıp iOS'ta da kullanılabilsin. Kimlik doğrulama akışı web'dekiyle birebir aynıdır: form-encoded login, 1 saatlik access token + günlerce geçerli refresh token, tek seferlik (single-flight) refresh interceptor.

**Tech Stack:**
- Kotlin 2.x, Jetpack Compose (BOM), Material 3, Navigation Compose
- Retrofit2 + OkHttp + kotlinx.serialization (JSON)
- Hilt (DI), DataStore + EncryptedSharedPreferences (token), Room (offline önbellek)
- MPAndroidChart (candlestick/grafikler — web'deki klinecharts'a en yakın olgun kütüphane), Coil (görseller)
- WorkManager (periyodik fiyat yenileme), FCM (Faz 5 — backend eklentisi gerekir)
- minSdk 26, targetSdk 35, Gradle Kotlin DSL + version catalog (`libs.versions.toml`)
- Lisans: AGPL-3.0 (org konvansiyonuyla uyumlu)

---

## 1. Karar: Neden Kotlin (Java değil)

"Java ile Kotlin" bir ikili değildir — Kotlin, JVM üzerinde Java ile **%100 birlikte çalışır** (interop). Yani ayrım "Java mı Kotlin mi?"dir:

- **Kotlin önerilir.** Google'ın resmi önerisi, null-güvenlik (crash'lerin büyük çoğunluğu null-pointer'dır), coroutine'ler (backend'deki async FastAPI doğasıyla doğal eşleşir), Compose ile modern UI. Daha az kod, daha az hata.
- Mevcut Java kod tabanın/lib'lerin varsa sorun değil: aynı projede karışık çalışır. Yeni yazılan her şey Kotlin olmalı.
- **Android tek hedefse:** Flutter/React Native yerine native Kotlin önerilir — platform entegrasyonu (widget, biyometri, FCM, performans) en iyisi budur.
- **İleride iOS istenirse:** Data/domain katmanı KMP'ye taşınır (bu plan zaten buna göre ayrıştırılmıştır); UI tarafı iOS'ta SwiftUI ile yeniden yazılır.

**Bu plandaki varsayım:** Android-first native Kotlin. iOS kararı sonradan verilirse data katmanı hazırdır.

---

## 2. Mevcut Durum / Uyumluluk Analizi (depolardan çıkarıldı)

### 2.1 Backend gerçekleri (project-florence/backend)

- FastAPI (Python), tüm rotalar `/api/v1` prefix'i altında, 17 router.
- **Prod'da /docs, /redoc, /openapi.json KAPALI** (`docs_enabled = not is_production()`). → OpenAPI şemasını **dev modunda** çalıştırıp `curl localhost:7055/openapi.json` ile al, Kotlin istemcisini oradan üret. (openapi-generator: `retrofit2` + `kotlinx-serialization` hedefi)
- **Auth akışı (web ile birebir uygulanacak):**
  - `POST /api/v1/auth/register` — JSON body
  - `POST /api/v1/auth/login` — **OAuth2PasswordRequestForm** = `application/x-www-form-urlencoded`, alanlar: `username`, `password` → yanıt: `{access_token, refresh_token, token_type}` (+ httpOnly cookie, mobilde kullanılmaz)
  - `POST /api/v1/auth/refresh` — JSON `{refresh_token}` → yeni `{access_token, refresh_token}`. **Rate limit: 5 istek/dakika (Redis)** → refresh'i asla döngüde çalıştırma, backoff uygula
  - `POST /api/v1/auth/logout` — JSON `{refresh_token}`
  - `DELETE /api/v1/auth/delete`, `PUT /auth/change-password|change-email|change-username`, `GET /profile`, `GET /credits`, `GET|PUT /user/preferences`
  - Access token: JWT HS256, **1 saat** ömür (`exp`), payload'da `user_id`
  - Refresh token: gün bazlı TTL (backend'de ayarlanır), veritabanında saklanır
  - Parolalar Argon2 ile hash'lenir (backend tarafı — mobilde sadece ilet)
- **CORS:** Prod'da yalnızca `tauri://localhost` originleri. **Native mobil istemci etkilenmez** (CORS tarayıcı konseptidir; OkHttp origin göndermez). WebView tabanlı bir çözüm seçilirse CORS devreye girer — native kalınmalı.
- **Public (auth'suz) uçlar:** `/auth/login`, `/auth/register`, `/auth/refresh`, `/auth/logout`, `/legal`, `/about`, `/contact`, `/version`, `/maintenance`, `/contributors`, `/health`, `/`
- Piyasa verisi uçlarının çoğu **opsiyonel auth** (`get_current_user_optional`) — misafir modu web'de var, mobilde de olmalı.
- Rate limiting Redis tabanlı; 429 dönerse hata ekranı + geri sayım göster.
- API base URL prod için: `https://api.florencex.com.tr` (web'in Tauri dalında kullanılan değer). Dev: `http://10.0.2.2:7055` (emülatör).

### 2.2 Web davranışı referansı (project-florence/web)

- axios istemcisi: istek öncesi `Authorization: Bearer <access>`, 401'de **tek seferlik refresh** (`refreshing` promise paylaşılır), başarısız refresh → logout/clear. Aynısı OkHttp `Authenticator` + `synchronized` ile yapılacak.
- Token saklama: tarayıcıda localStorage / Tauri'de güvenli store → mobilde EncryptedSharedPreferences (refresh) + bellek (access).
- React Query = veri önbelleği → mobilde Room + `StateFlow` cache-first stratejisi.
- i18n: `tr`/`en` (web'de i18next). Mobilde aynı iki dil.
- Tema: 6 el yapımı tema (Florence, Ocean, Emerald, Midnight, Sunset, Light) + koyu/açık. Mobilde Material 3 color scheme'lerine eşlenir.
- Web özellikleri (mobilde karşılanacak): pazar panosu, AI danışman, mum grafikler, otomatik raporlar, simülasyonlar, sanal portföyler, takip listesi/favoriler, duyurular, çok dilli arayüz, temalar.

### 2.3 API uç kataloğu (mobil kapsam eşlemesi)

| Router | Uçlar | Mobil v1 kapsamı |
|---|---|---|
| auth | register, login, refresh, logout, delete, change-password/email/username, profile, credits, user/preferences | ✅ tamamı |
| bist | companies, tickers, companies/search, companies/info/{ticker}, companies/info/{ticker}/md, companies/summary, news/{ticker}, price/history/{ticker}, price/current | ✅ tamamı |
| economy | gold-prices, silver-price, gram-platinum-price, gram-palladium-price, currency, macroeconomy | ✅ |
| ipo | ipos/upcoming, ipos/draft, ipos/active, ipos/{slug} | ✅ |
| stats | stats/top, stats/{ticker} | ✅ |
| favorites | POST/DELETE/GET favorites/{ticker} | ✅ |
| portfolio | POST portfolio/profile (risk profili) | ✅ (AI danışman girişi) |
| reports | generate, info, history, search, {id}, download | ✅ (kredi tabanlı — maliyet uyarısı UI'da) |
| simulations | per-day-cost, estimate-cost/{ticker}, history, history/{sim_id}, {ticker} | ✅ |
| virtual_portfolio | CRUD portfolio + transactions + valuation/diversification/performers/history/returns/risk/benchmark/performance/stats/snapshot + export/csv | ✅ tamamı |
| announcements | list, {id}, read (post/put/delete admin) | ✅ (list/detail/read) |
| export | user/export | ✅ (dosya indir + paylaş) |
| analytics | analytics/event | ✅ (telemetri) |
| legal | legal, legal/all, about, contact, version, maintenance | ✅ |
| contributors | contributors | ✅ (hakkında ekranı) |

**Toplam ~70 uç.** Tümü REST/JSON; dosya indirme: `virtual_portfolio/{id}/export/csv` ve `reports/download` PlainText/PDF yanıtı.

---

## 3. Önerilen Repo Yapısı

Yeni repo: `project-florence/mobile` (org konvansiyonu). Kök:

```
mobile/
├── AGENTS.md                  # backend/web'deki gibi (agent çalışma kuralları)
├── LICENSE                    # AGPL-3.0
├── README.md                  # TR + EN
├── .github/workflows/         # CI: lint + unit test + assemble
├── gradle/libs.versions.toml
├── settings.gradle.kts
├── build.gradle.kts
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── java/com/florence/app/
│       │   │   ├── FlorenceApp.kt          # Application, Hilt
│       │   │   ├── MainActivity.kt
│       │   │   ├── core/                    # network, datastore, theme, i18n, di
│       │   │   ├── data/                    # api (Retrofit), model (DTO), cache (Room), repository
│       │   │   ├── domain/                  # use case'ler, model
│       │   │   └── presentation/            # navigation, screens, components, viewmodel
│       │   └── res/
│       ├── test/               # MockWebServer + JUnit4
│       └── androidTest/        # Compose UI testleri
```

---

## 4. Faz Planı (her faz → görevler)

### FAZ 0 — Sözleşme & Proje İskeleti

**Görev 0.1:** Backend'i dev modunda ayağa kaldır (repo kökünde `docker-compose.yml` + `install.sh` mevcut; `.env`'i `.env.example`'dan kopyala) ve `curl http://localhost:7055/openapi.json > docs/openapi.json` ile şemayı al. **Doğrulama:** JSON geçerli, `paths` anahtarı ~70 uç içeriyor.
> Not: `[llm_client] custom_url=http://localhost:7777/v1` ve embedding (Ollama) olmadan da auth + veri uçları çalışır; rapor/simülasyon üretimi LLM gerektirir — v1'de bu uçlar manuel test edilir.

**Görev 0.2:** Android Studio ile yeni proje (`com.florence.app`), minSdk 26 / targetSdk 35, Empty Compose Activity. Gradle Kotlin DSL + `libs.versions.toml` taşı. **Doğrulama:** `./gradlew assembleDebug` başarılı.

**Görev 0.3:** Bağımlılıklar: Retrofit, OkHttp (logging), kotlinx-serialization-converter, Hilt, Room, DataStore, Navigation Compose, Coil, MPAndroidChart, MockWebServer (test). **Doğrulama:** derleme geçer.

**Görev 0.4:** `core/network` — `FlorenceApiService` (Retrofit arayüzü, tüm uçlar — openapi.json'dan üretilen şema + elle düzeltme), `AuthInterceptor` (Bearer ekleme), `AuthRefreshAuthenticator` (tek seferlik refresh). **Doğrulama:** MockWebServer testi: 401 → refresh çağrılır → orijinal istek tekrarlanır; refresh 429/401 olursa logout.

**Görev 0.5:** Tema sistemi — web'deki 6 temanın Material 3 renk eşlemesi (`core/theme`), sistem koyu/açık desteği, tema seçimi DataStore'da. **Doğrulama:** tema değişimi anında yansıyor.

**Görev 0.6:** i18n — `tr`/`en` string kaynakları (`values/`, `values-tr/`); web'in tr.json/en.json kopyalanıp uyarlanır. **Doğrulama:** dil değiştirince tüm ekranlar dönüyor.

**Görev 0.7:** Build variant'ları: `dev` (10.0.2.2:7055) / `prod` (api.florencex.com.tr) — `BuildConfig.API_BASE_URL`. **Doğrulama:** dev variant'ta emülatörden `GET /api/v1/version` çalışıyor.

### FAZ 1 — Kimlik Doğrulama & Oturum

**Görev 1.1:** `data/api/AuthApi.kt` — `@FormUrlEncoded login(username, password)`, `register(UserRegister)`, `refresh(refreshToken)`, `logout`, `changePassword/Email/Username`, `deleteAccount`, `getProfile`, `getCredits`, `get/putPreferences`. **Doğrulama:** MockWebServer — form-encoded body ve yanıt ayrıştırma testleri yeşil.

**Görev 1.2:** `TokenStore` — EncryptedSharedPreferences'ta refresh token, bellekte access token (`MemoryTokenStore`), `Flow` ile oturum durumu. **Doğrulama:** unit test — token yaz/oku/temizle.

**Görev 1.3:** Login/Register ekranları (Compose) — form validasyonu, hata gösterimi (429/401/network ayrımı), "beni hatırla" yok (refresh token zaten uzun ömürlü). **Doğrulama:** backend dev'de gerçek kayıt + giriş.

**Görev 1.4:** Oturum yönlendirme — `AuthState` (Yükleniyor / Misafir / Girişli); Navigation Compose ile korumalı rotalar; 401'de refresh denemesi, başarısızsa login ekranına. **Doğrulama:** token'ı elle bozup app'i yeniden başlat → login'e düşmeli.

**Görev 1.5:** Misafir modu — auth'suz veri uçları (bist/economy/ipo/stats) girişsiz kullanılabilir; favori/portföy/rapor giriş ister (web'deki `ProtectedRoute` davranışı). **Doğrulama:** çıkış yapılı halde pano görünüyor, favori eklemek login'e yönlendiriyor.

### FAZ 2 — Piyasa Verileri (Çekirdek Ekranlar)

**Görev 2.1:** Pano — `GET /bist/tickers`, `/economy/currency`, `/economy/gold-prices`, `/price/current` birleşik görünüm; web'deki widget grid'ine karşılık gelen düzen (hisse / döviz / metal kartları). Auto-refresh (5 dk, WorkManager veya tek seferlik coroutine). **Doğrulama:** emülatörde canlı veri + yenileme.

**Görev 2.2:** Hisse arama — `GET /companies/search` + `GET /bist/companies` (önbelleğe alınır); debounce'lu arama, sonuç listesi. **Doğrulama:** "THYAO", "garanti" gibi sorgular sonuç döndürüyor.

**Görev 2.3:** Şirket detay — `companies/info/{ticker}`, `companies/info/{ticker}/md` (temel veri), `price/history/{ticker}?period=&interval=` (5m/30m/1h/1d mumlar), MPAndroidChart candlestick + zaman aralığı seçici (web'deki klinecharts davranışı), `stats/{ticker}`, `news/{ticker}` (haber listesi). **Doğrulama:** THYAO için grafik çiziliyor, aralık değişince yeniden yükleniyor.

**Görev 2.4:** Favoriler/takip listesi — `GET/POST/DELETE /favorites/{ticker}`; liste ekranı canlı fiyatlarla (`price/current` ile zenginleştir), boş durum + çekme ile yenileme. **Doğrulama:** favori ekle/sil/listele uçtan uca.

**Görev 2.5:** Ekonomi ekranı — `economy/currency`, `gold-prices`, `silver-price`, `gram-platinum-price`, `gram-palladium-price`, `macroeconomy`; kur tablosu + metal kartları. **Doğrulama:** tüm uçlar veri döndürüyor.

**Görev 2.6:** IPO takvimi — `ipos/upcoming`, `ipos/active`, `ipos/draft`, `ipos/{slug}` detay. **Doğrulama:** liste + detay navigasyonu.

### FAZ 3 — Sanal Portföy, Raporlar, Simülasyon & AI Danışman

**Görev 3.1:** Portföy CRUD — `virtual_portfolio` POST/GET/PUT/DELETE + `duplicate`; liste ve detay ekranları. **Doğrulama:** portföy oluştur/düzenle/sil.

**Görev 3.2:** İşlemler — `POST/PUT/DELETE /{portfolio_id}/transactions...` (al/sat, komisyon `PORTFOLIO_COMMISSION_RATE=0.001` web'deki `PortfolioBuySell` davranışı); işlem sonrası `valuation` + `returns` güncellenir. **Doğrulama:** al-sat kaydı → değerleme değişiyor.

**Görev 3.3:** Performans & analitik — `performance`, `benchmark` (XU100 karşılaştırma grafiği), `diversification`, `risk`, `performers`, `history`, `stats`, `snapshot`. **Doğrulama:** grafikler veriyle doluyor.

**Görev 3.4:** CSV dışa aktarma — `export/csv` → dosya indir + Android paylaşım sayfası (`FileProvider`). **Doğrulama:** indirilen CSV açılıyor.

**Görev 3.5:** Raporlar — `reports/generate` (tip seçimi + kredi maliyeti önizleme — web'deki `CreditCostTooltip` gibi uyarı), `reports/history`, `reports/search`, `reports/{id}` (markdown render — Compose'da markdown için `com.mikepenz:multiplatform-markdown-renderer` veya HTML dönüşümü), `reports/download`. **Doğrulama:** 1 rapor üret → tarihçede görün → içerik render.

**Görev 3.6:** Simülasyonlar — `simulations/per-day-cost`, `estimate-cost/{ticker}`, `{ticker}` başlat, `history` + `history/{sim_id}`; maliyet onayı akışı. **Doğrulama:** simülasyon başlat ve sonucu görüntüle.

**Görev 3.7:** AI Danışman — `POST /stocks/fit` (risk toleransı + yatırım ufku + kârlılık formu) → öneri listesi (`RecommendationsGauge` benzeri görsel); risk profilini `POST /portfolio/profile` ile kaydet. **Doğrulama:** form doldur → öneriler geliyor.

### FAZ 4 — Hesap & Sistem Ekranları

**Görev 4.1:** Profil ekranı — `profile`, `credits` (kredi göstergesi + dolum bilgisi: `FREE_CREDIT_MAX=25`, `DAILY_FREE_CREDIT_REFILL=5` backend'den gelir), şifre/e-posta/kullanıcı adı değiştirme diyalogları, hesap silme (onaylı). **Doğrulama:** tüm mutasyonlar gerçek backend'de çalışıyor.

**Görev 4.2:** Duyurular — `announcements` listesi + `{id}` detay + `read` işareti (yerel bildirimle değil, rozetle). **Doğrulama:** duyuru okununca rozet kayboluyor.

**Görev 4.3:** Veri dışa aktarma — `GET /user/export` (kullanıcı verisi) indir + paylaş. **Doğrulama:** JSON indiriliyor.

**Görev 4.4:** Yasal & sistem — `legal`, `legal/all`, `about`, `contact`, `contributors`, `version`, `maintenance` (bakım modu ekranı — uygulama açılışında `maintenance` kontrolü, aktifse bilgi ekranı). **Doğrulama:** her ekran backend'den içerik çekiyor.

**Görev 4.5:** Telemetri — `POST /analytics/event` (ekran görüntüleme olayları; web'deki `usePageTracking` eşleniği), kullanıcı izni ile. **Doğrulama:** event log'da görünüyor.

### FAZ 5 — Offline, Push, Widget & Yayın

**Görev 5.1:** Room önbelleği — bist/companies, fiyat geçmişi, ekonomik veriler cache-first (web'deki React Query + TTL davranışı: `price_history.cache_ttl=60`, `economy.cache_ttl=1200` backend'den dönen değerlerle uyumlu). Uçuş modunda pano okunabilir + "son güncelleme" damgası. **Doğrulama:** uçak modunda eski veri görünüyor.

**Görev 5.2:** Push bildirimleri — **BACKEND EKLENTİSİ GEREKİR** (backend'de FCM endpoint'i yok): `POST /api/v1/push/register` (device token) ucu backend'e eklenir + duyuru oluşturulunca FCM tetiklenir. v1'de alternatif: duyuru çekme + yerel bildirim (WorkManager 15 dk). **Doğrulama:** duyuru yayınlanınca bildirim geliyor (veya v1'de 15 dk içinde yerel bildirim).

**Görev 5.3:** Android widget — takip listesi widget'ı (AppWidgetProvider, 30 dk güncelleme — `price_history.cache_ttl_hot=604800` dikkate alınarak). **Doğrulama:** ana ekrana widget → güncel fiyatlar.

**Görev 5.4:** Biyometrik kilit — BiometricPrompt ile uygulama kilidi (opsiyonel, ayarlardan aç/kapa). **Doğrulama:** kilit açıkken app açılışında parmak izi istenir.

**Görev 5.5:** Test & kalite — ktlint/detekt, MockWebServer ile tüm repository'lerin unit testleri, kritik ekranların Compose UI testleri, CI (GitHub Actions: lint + test + assembleRelease). **Doğrulama:** `./gradlew test lint assembleRelease` yeşil.

**Görev 5.6:** Yayın — Play Store: uygulama imzalama, AAB, fiyatlandırma (ücretsiz), gizlilik politikası (backend `/legal/privacy_policy` içeriğinden), AGPL-3.0 kaynak erişimi notu. **Doğrulama:** Play Console'da dahili test sürümü canlı.

---

## 5. Kritik Uyumluluk Notları (uygularken)

1. **Login body'si form-encoded'tır** (OAuth2PasswordRequestForm) — JSON değil. `@FormUrlEncoded @Field("username")`.
2. **Access token 1 saat** — kısa. `AuthRefreshAuthenticator` zorunlu; refresh **single-flight** ve rate-limit uyumlu (5/dk → ardışık 401'lerde 30 sn soğuma, asla döngü).
3. **Prod'da OpenAPI kapalı** — şemayı dev'den al, `docs/openapi.json` olarak repo'ya koy; değişikliklerde backend ile senkron tut.
4. **CORS yok** — native OkHttp için sorun değil; WebView'e geçilirse yeniden düşün.
5. **Kredi ekonomisi** — rapor/simülasyon kredi harcar; üretmeden önce maliyeti göster (web'deki `CreditCostTooltip` davranışı), `credits` ucuyla bakiye göster.
6. **Opsiyonel auth** — misafir modu destekle; istek 401 dönerse kullanıcıyı sessizce login'e atmak yerine "giriş gerekli" akışı.
7. **Bakım modu** — `GET /maintenance` yanıtına göre uygulamayı bilgi ekranına kilitle.
8. **TTL değerleri** — backend cache TTL'leri (ör. fiyat 60 sn, ekonomi 20 dk) mobil önbellek politikasının üst sınırıdır; daha sık yoklama anlamsız.
9. **i18n** — yeni metinler hem tr hem en'e yazılır.
10. **AGPL-3.0** — org'daki tüm depolar bu lisansı kullanıyor; mobile de aynısı.

---

## 6. Test / Doğrulama Stratejisi

- **Unit:** JUnit4 + MockWebServer — her repository'nin başarı/401/429/network-hatası yolları; TokenStore; ViewModel durumları (StateFlow).
- **UI:** Compose UI testleri (login formu, pano yükleme, favori akışı).
- **Uçtan uca (manuel):** backend `docker-compose up` → emülatör `dev` variant → kayıt/giriş → pano → portföy → rapor.
- **CI:** GitHub Actions — `lint` + `testDebugUnitTest` + `assembleDebug` (her PR).
- LLM gerektiren uçlar (rapor üret, simülasyon) ortamda LLM yoksa: mock yanıtlarla test, canlı test ayrı not edilir.

---

## 7. Riskler & Açık Sorular

| Risk / Soru | Etki | Karar / Not |
|---|---|---|
| Push için backend'de FCM ucu yok | v1'de push yok; çekme + yerel bildirim | Faz 5'te backend'e `POST /push/register` eklenmesi önerilir (ayrı PR) |
| iOS istenirse | UI yeniden yazılır (SwiftUI), data katmanı KMP'ye taşınır | Bu plan data katmanını buna hazır tutar |
| LLM/embedding altyapısı olmadan rapor testi | Dev ortamında rapor uçları hata verebilir | Mock ile test; canlı test üretim ortamında |
| Rapor markdown render | Compose'ta markdown desteği sınırlı | `multiplatform-markdown-renderer` veya HTML'den `AndroidView` WebView (yalnızca içerik, CORS sorunu yok — veri zaten çekilmiş) |
| Kredi maliyeti şeffaflığı | Kullanıcı şikayeti | Üretim öncesi onay diyaloğu zorunlu |
| `api.florencex.com.tr` prod adresi doğrulanmalı | Uygulama yayın öncesi hangi base URL? | Yayın öncesi backend sahibiyle teyit |
| Uygulama adı/logo | Marka | web'deki `florence_logo.svg` varlıkları kullanılır |

**Repo:** `project-florence/mobile` (öneri). İstersen ben bu plana göre iskeleti (Faz 0-1) şimdi kurabilirim.
