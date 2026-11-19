package com.respondo.data.cloud

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object CloudNetworking {
    fun service(baseUrl: String, client: OkHttpClient? = null): CloudApiService? {
        if (baseUrl.isBlank()) return null
        require(baseUrl.startsWith("https://") && baseUrl.endsWith("/")) {
            "Backend URL must use HTTPS and end with /"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client ?: newClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudApiService::class.java)
    }

    /** Supplies the shared timeout/security defaults and permits future auth interceptors. */
    fun newClient(configure: OkHttpClient.Builder.() -> Unit = {}): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .followSslRedirects(false)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)
        builder.configure()
        return builder.build()
    }
}
