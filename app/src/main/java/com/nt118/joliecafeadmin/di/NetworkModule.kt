package com.nt118.joliecafeadmin.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nt118.joliecafeadmin.data.network.FCMApi
import com.nt118.joliecafeadmin.data.network.JolieAdminApi
import com.nt118.joliecafeadmin.util.Constants.Companion.BASE_URL
import com.nt118.joliecafeadmin.util.Constants.Companion.FCM_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideHttpClient() : OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS) // connect timeout
            .writeTimeout(20, TimeUnit.SECONDS) // write timeout
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private var json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    @Singleton
    @Provides
    fun provideConverterFactory(): Converter.Factory {
        val contentType = "application/json".toMediaType()
        return json.asConverterFactory(contentType)
    }


    @Singleton
    @Provides
    @Named("JolieAdmin")
    fun provideRetrofitInstance(
        okHttpClient: OkHttpClient,
        jsonConverterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(jsonConverterFactory)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    @Named("FCM")
    fun provideFCMRetrofitInstance(
        okHttpClient: OkHttpClient,
        jsonConverterFactory: Converter.Factory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FCM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(jsonConverterFactory)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideApiService( @Named("JolieAdmin") retrofit: Retrofit): JolieAdminApi {
        return retrofit.create(JolieAdminApi::class.java)
    }

    @Singleton
    @Provides
    fun provideFCMApiService(@Named("FCM") retrofit: Retrofit): FCMApi {
        return retrofit.create(FCMApi::class.java)
    }
}