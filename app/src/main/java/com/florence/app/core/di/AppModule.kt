package com.florence.app.core.di

import com.florence.app.BuildConfig
import com.florence.app.core.network.AuthInterceptor
import com.florence.app.core.network.TokenRefreshAuthenticator
import com.florence.app.core.storage.TokenStore
import com.florence.app.data.api.AuthEndpoints
import com.florence.app.data.api.FlorenceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    /**
     * Refresh için ayrı, bağımsız OkHttp istemcisi.
     * Üzerinde AuthInterceptor/Authenticator YOK — böylece Hilt grafiğinde döngü
     * oluşmaz ve refresh çağrısı kendini asla tetiklemez.
     */
    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    @Named("refresh")
    fun provideRefreshRetrofit(
        @Named("refresh") client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideAuthEndpoints(@Named("refresh") retrofit: Retrofit): AuthEndpoints =
        retrofit.create(AuthEndpoints::class.java)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenStore: TokenStore,
        authApi: AuthEndpoints,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenStore))
        .authenticator(TokenRefreshAuthenticator(tokenStore, authApi))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideFlorenceApi(retrofit: Retrofit): FlorenceApi =
        retrofit.create(FlorenceApi::class.java)
}
