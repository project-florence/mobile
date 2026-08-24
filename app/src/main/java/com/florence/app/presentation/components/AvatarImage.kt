package com.florence.app.presentation.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.florence.app.BuildConfig
import com.florence.app.core.net.avatarUrl

/**
 * Backend'den servis edilen gerçek avatar SVG'sini Coil ile yükler.
 *
 * - `url` göreceli (`/avatars/x.svg`) ya da mutlak olabilir;
 *   [avatarUrl] ile BuildConfig.API_BASE_URL üzerinden birleştirilir.
 * - Yükleme/şifre çözme başarısız olduğunda (ağ yok, 404 vb.) eski
 *   renkli [AvatarArt] yer tutucusuna düşer, böylece UI asla boş kalmaz.
 * - SVG dekodlama, `coil-svg` artefaktı tarafından otomatik kaydedilen
 *   SvgDecoder ile yapılır (network için `coil-network-okhttp` gerekir).
 */
@Composable
fun AvatarImage(
    url: String?,
    avatarId: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    val fullUrl = url
        ?.takeIf { it.isNotBlank() }
        ?.let { avatarUrl(BuildConfig.API_BASE_URL, it) }

    val fallback: @Composable () -> Unit = {
        AvatarArt(avatarId = avatarId, size = size, showLabel = showLabel)
    }

    if (fullUrl == null) {
        fallback()
        return
    }

    val context = LocalContext.current
    val request = ImageRequest.Builder(context)
        .data(fullUrl)
        .crossfade(true)
        .build()

    SubcomposeAsyncImage(
        model = request,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .clip(CircleShape),
        loading = { fallback() },
        error = { fallback() },
    )
}