package com.respondo.data.cloud

data class ApiEnvelopeDto<T>(
    val success: Boolean? = null,
    val data: T? = null,
    val error: ApiErrorDto? = null,
)

data class ApiErrorDto(val code: String? = null, val message: String? = null)

data class HealthDto(
    val status: String? = null,
    val service: String? = null,
    val timestamp: String? = null,
    val apiVersion: String? = null,
)

data class SystemInfoDto(
    val application: String? = null,
    val apiVersion: String? = null,
    val environment: String? = null,
    val capabilities: List<String>? = null,
)

data class DatabaseStatusDto(val status: String? = null, val checkedAt: String? = null)
