package com.florence.app.core.net

/**
 * Backend avatarları göreceli URL döner (örn. `/avatars/x.svg`); burada API
 * origin (BuildConfig.API_BASE_URL) ile mutlak bir URL'ye birleştirilir.
 *
 * `base` sonunda `/` olsun olmasın güvenli çalışır: `https://api.florencex.com.tr/`
 * + `/avatars/x.svg` → `https://api.florencex.com.tr/avatars/x.svg`.
 *
 * Zaten mutlak (http/https) gelen url olduğu gibi döndürülür.
 */
fun avatarUrl(base: String, url: String): String =
    if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        base.trimEnd('/') + "/" + url.trimStart('/')
    }