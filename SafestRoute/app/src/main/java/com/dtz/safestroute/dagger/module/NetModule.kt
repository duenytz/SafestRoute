package com.dtz.clinic.dagger.module

import com.dtz.clinic.net.service.RouteService
import com.dtz.safestroute.BuildConfig
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Singleton

@Module class NetModule {

    companion object {
        private val CONNECT_TIMEOUT: Long = 15
        private val READ_TIMEOUT: Long = 10
        private val WRITE_TIMEOUT: Long = 10
        private val DUMMY_URL = "https://route.api.here.com/routing/7.2/"
    }

    @Provides @Singleton internal fun provideOkHttpClient(): OkHttpClient =
            OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(
                            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC))
                    .connectTimeout(CONNECT_TIMEOUT, SECONDS)
                    .readTimeout(READ_TIMEOUT, SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, SECONDS)
                    .build()

    @Provides @Singleton internal fun provideGson(): Gson =
            GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT)
                    .setPrettyPrinting()
                    .create()

    @Provides @Singleton internal fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
            Retrofit.Builder().baseUrl(DUMMY_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()

    @Provides @Singleton internal fun provideWebService(retrofit: Retrofit): RouteService =
            retrofit.create(RouteService::class.java)
}
