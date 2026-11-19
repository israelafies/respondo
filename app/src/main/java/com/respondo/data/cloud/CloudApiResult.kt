package com.respondo.data.cloud

sealed interface CloudApiResult<out T> {
    data class Success<T>(val value: T) : CloudApiResult<T>
    data class HttpFailure(val statusCode: Int, val code: String? = null) : CloudApiResult<Nothing>
    data class NetworkFailure(val kind: NetworkFailureKind) : CloudApiResult<Nothing>
    data object InvalidResponse : CloudApiResult<Nothing>
    data object NotConfigured : CloudApiResult<Nothing>
}

enum class NetworkFailureKind { DNS, TIMEOUT, CONNECTION, UNKNOWN }
