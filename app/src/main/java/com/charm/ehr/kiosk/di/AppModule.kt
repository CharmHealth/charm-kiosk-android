package com.charm.ehr.kiosk.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.preference.PreferenceManager
import coil.ImageLoader
import coil.disk.DiskCache
import com.charm.ehr.kiosk.BuildConfig
import com.charm.ehr.kiosk.common.util.AuthenticationUtil
import com.charm.ehr.kiosk.common.util.MDAccountsRelatedUtil
import com.charm.ehr.kiosk.data.network.WebService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(appContext)


    @Provides
    fun provideGson(): Gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()

    @Provides
    fun providesGsonConverterFactory(gson: Gson): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }

    @Provides
    fun provideBaseUrl(): String {
        return MDAccountsRelatedUtil.baseUrl
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): WebService {
        return retrofit.create(WebService::class.java)
    }

    @Provides
    fun providesCommonInterceptor(): Interceptor = Interceptor { chain ->
        val request: Request = chain.request().newBuilder().run {
            addHeader("Accept", "application/json")
            addHeader("app_user_agent", getAppUserAgent())
            build()
        }
        if (BuildConfig.DEBUG) {
            Log.d("mdCall", request.url.toString())
        }
        chain.proceed(request)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(interceptor: Interceptor, authenticationUtil: AuthenticationUtil): OkHttpClient {
        return OkHttpClient().newBuilder()
            .addInterceptor(interceptor)
            // Sign-in problems surface through AuthenticationUtil.apiAuthFailureOf (see CharmAPI).
            .addInterceptor(authenticationUtil.authInterceptor())
            .also {
            it.connectTimeout(3, TimeUnit.MINUTES)
            it.writeTimeout(3, TimeUnit.MINUTES)
            it.readTimeout(3, TimeUnit.MINUTES)
            if (BuildConfig.DEBUG) {
                it.addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                    redactHeader("Authorization")
                })
            }
        }.build()
    }

    @Provides
    @Singleton
    fun providesRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
        baseUrl: String,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }


    @Provides
    @Singleton
    fun provideImageLoader(
        okHttpClient: OkHttpClient,
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.10) // 10% of the app's available disk space
                    .build()
            }
            .build()
    }

    private fun getAppUserAgent(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val userAgent = "Charm_Kiosk/${Build.VERSION.RELEASE}(Android ${Build.VERSION.RELEASE}, ${
            if (model.lowercase()
                    .startsWith(manufacturer.lowercase())
            ) model else "$manufacturer $model"
        })"
        return userAgent
    }

}

