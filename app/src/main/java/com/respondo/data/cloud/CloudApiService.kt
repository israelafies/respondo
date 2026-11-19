package com.respondo.data.cloud

import retrofit2.Response
import retrofit2.http.GET

interface CloudApiService {
    @GET("health")
    suspend fun health(): Response<ApiEnvelopeDto<HealthDto>>

    @GET("api/v1/system/info")
    suspend fun systemInfo(): Response<ApiEnvelopeDto<SystemInfoDto>>

    @GET("api/v1/system/database")
    suspend fun databaseStatus(): Response<ApiEnvelopeDto<DatabaseStatusDto>>
}
