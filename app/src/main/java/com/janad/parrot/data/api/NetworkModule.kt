package com.janad.parrot.data.api

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.janad.parrot.data.UserPreferences
import com.janad.parrot.data.getAccessTokenBlocking
import com.janad.parrot.data.getRefreshTokenBlocking
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    internal const val BASE_URL = "https://your-server-api-url.com/"
    internal const val IMAGES_PATH = BASE_URL+"images/"
    internal const val VIDEOS_PATH = BASE_URL+"videos/"
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    // Base Retrofit (no auth) for creating AuthApi
    @Provides
    @Singleton
    @BaseRetrofit
    fun provideBaseRetrofit(json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(@BaseRetrofit baseRetrofit: Retrofit): AuthApi {
        return baseRetrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        userPreferences: UserPreferences,
        authApi: AuthApi
    ): OkHttpClient {
        val authInterceptor = AuthInterceptor(
            tokenProvider = { userPreferences.getAccessTokenBlocking() },
            refreshTokenProvider = { userPreferences.getRefreshTokenBlocking() },
            onNewAccessToken = { newToken ->
                runBlocking { userPreferences.saveAccessToken(newToken) }
            },
            refreshCall = { refreshToken ->
                // call AuthApi synchronously

                    authApi.refreshToken(RefreshReq(refreshToken))

            }
        )

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Main Retrofit (with AuthInterceptor) - this is the default one injected
    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(@AuthRetrofit retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}