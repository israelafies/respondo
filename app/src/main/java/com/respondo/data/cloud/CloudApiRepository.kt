package com.respondo.data.cloud

import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.EOFException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface CloudApiRepository {
    suspend fun checkBackendHealth(): CloudApiResult<HealthDto>
    suspend fun getSystemInformation(): CloudApiResult<SystemInfoDto>
    suspend fun checkDatabaseConnectivity(): CloudApiResult<DatabaseStatusDto>
}

class RetrofitCloudApiRepository(
    private val service: CloudApiService?,
) : CloudApiRepository {
    override suspend fun checkBackendHealth() = request({ service?.health() }) {
        it.status == "ok" && !it.service.isNullOrBlank() && !it.timestamp.isNullOrBlank() && !it.apiVersion.isNullOrBlank()
    }
    override suspend fun getSystemInformation() = request({ service?.systemInfo() }) {
        !it.application.isNullOrBlank() && !it.apiVersion.isNullOrBlank() &&
            !it.environment.isNullOrBlank() && it.capabilities != null
    }
    override suspend fun checkDatabaseConnectivity() = request({ service?.databaseStatus() }) {
        it.status == "connected" && !it.checkedAt.isNullOrBlank()
    }

    private suspend fun <T : Any> request(
        call: suspend () -> Response<ApiEnvelopeDto<T>>?,
        isValid: (T) -> Boolean,
    ): CloudApiResult<T> {
        if (service == null) return CloudApiResult.NotConfigured
        return try {
            val response = call() ?: return CloudApiResult.NotConfigured
            val body = response.body()
            if (!response.isSuccessful) {
                val error = response.errorBody()?.use {
                    runCatching {
                        JsonParser.parseString(it.string())
                            .takeIf { json -> json.isJsonObject }
                            ?.asJsonObject
                            ?.getAsJsonObject("error")
                            ?.get("code")
                            ?.takeUnless { code -> code.isJsonNull }
                            ?.asString
                    }.getOrNull()
                }
                return CloudApiResult.HttpFailure(response.code(), error)
            }
            val envelope = body ?: return CloudApiResult.InvalidResponse
            val data = envelope.data ?: return CloudApiResult.InvalidResponse
            if (envelope.success != true || !isValid(data)) CloudApiResult.InvalidResponse
            else CloudApiResult.Success(data)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: JsonParseException) {
            CloudApiResult.InvalidResponse
        } catch (_: MalformedJsonException) {
            CloudApiResult.InvalidResponse
        } catch (_: EOFException) {
            CloudApiResult.InvalidResponse
        } catch (timeout: SocketTimeoutException) {
            CloudApiResult.NetworkFailure(NetworkFailureKind.TIMEOUT)
        } catch (dns: UnknownHostException) {
            CloudApiResult.NetworkFailure(NetworkFailureKind.DNS)
        } catch (_: IOException) {
            CloudApiResult.NetworkFailure(NetworkFailureKind.CONNECTION)
        } catch (_: Exception) {
            CloudApiResult.InvalidResponse
        }
    }
}
