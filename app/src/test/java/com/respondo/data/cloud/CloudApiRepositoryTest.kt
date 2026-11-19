package com.respondo.data.cloud

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.net.UnknownHostException

class CloudApiRepositoryTest {
    private val server = MockWebServer()
    private lateinit var repository: CloudApiRepository

    @Before fun setUp() {
        server.start()
        val client = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(300, TimeUnit.MILLISECONDS)
            .readTimeout(300, TimeUnit.MILLISECONDS)
            .callTimeout(600, TimeUnit.MILLISECONDS)
            .build()
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudApiService::class.java)
        repository = RetrofitCloudApiRepository(service)
    }

    @After fun tearDown() { server.shutdown() }

    @Test fun parsesSuccessfulWorkerEndpoints() = runBlocking {
        server.enqueue(json("""{"success":true,"data":{"status":"ok","service":"respondo-api","timestamp":"now","apiVersion":"1.0.0"},"error":null}"""))
        server.enqueue(json("""{"success":true,"data":{"application":"Respondo","apiVersion":"1.0.0","environment":"test","capabilities":["health","d1_connectivity"]},"error":null}"""))
        server.enqueue(json("""{"success":true,"data":{"status":"connected","checkedAt":"now"},"error":null}"""))

        assertEquals("ok", (repository.checkBackendHealth() as CloudApiResult.Success).value.status)
        assertEquals("Respondo", (repository.getSystemInformation() as CloudApiResult.Success).value.application)
        assertEquals("connected", (repository.checkDatabaseConnectivity() as CloudApiResult.Success).value.status)
        val requests = (1..3).map { server.takeRequest() }
        assertTrue(requests.all { it.method == "GET" })
        assertEquals(listOf("/health", "/api/v1/system/info", "/api/v1/system/database"), requests.map { it.path })
    }

    @Test fun reportsHttpErrorsWithoutExposingResponseBody() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setHeader("Content-Type", "application/json")
            .setBody("""{"success":false,"data":null,"error":{"code":"NOT_FOUND","message":"private details"}}"""))
        val result = repository.checkBackendHealth() as CloudApiResult.HttpFailure
        assertEquals(404, result.statusCode)
        assertEquals("NOT_FOUND", result.code)

        server.enqueue(MockResponse().setResponseCode(500).setBody("not-json"))
        assertEquals(500, (repository.getSystemInformation() as CloudApiResult.HttpFailure).statusCode)
    }

    @Test fun handlesInvalidJsonAndMalformedEnvelope() = runBlocking {
        server.enqueue(MockResponse().setBody("not-json"))
        assertTrue(repository.checkBackendHealth() is CloudApiResult.InvalidResponse)
        server.enqueue(json("""{"success":true,"data":null,"error":null}"""))
        assertTrue(repository.checkBackendHealth() is CloudApiResult.InvalidResponse)
        server.enqueue(json("""{"success":true,"data":{},"error":null}"""))
        assertTrue(repository.checkBackendHealth() is CloudApiResult.InvalidResponse)
    }

    @Test fun handlesTimeoutAndConnectionFailure() = runBlocking {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))
        val timeout = repository.checkBackendHealth()
        assertEquals(NetworkFailureKind.TIMEOUT, (timeout as CloudApiResult.NetworkFailure).kind)

        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        val failure = repository.checkBackendHealth()
        assertTrue(failure is CloudApiResult.NetworkFailure)
    }

    @Test fun classifiesDnsFailure() = runBlocking {
        val unreachable = Retrofit.Builder()
            .baseUrl("http://respondo.invalid/")
            .client(OkHttpClient.Builder()
                .dns { throw UnknownHostException("test DNS failure") }
                .callTimeout(2, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CloudApiService::class.java)
        val result = RetrofitCloudApiRepository(unreachable).checkBackendHealth()
        assertEquals(NetworkFailureKind.DNS, (result as CloudApiResult.NetworkFailure).kind)
    }

    @Test fun cancellationIsPropagatedToTheCaller() = runBlocking {
        server.enqueue(MockResponse()
            .setHeader("Content-Type", "application/json")
            .setBodyDelay(5, TimeUnit.SECONDS)
            .setBody("""{"success":true,"data":{"status":"ok","service":"respondo-api","timestamp":"now","apiVersion":"1.0.0"},"error":null}"""))
        val request = launch { repository.checkBackendHealth() }
        server.takeRequest()
        request.cancelAndJoin()
        assertTrue(request.isCancelled)
    }

    @Test fun unconfiguredBackendIsReportedExplicitly() = runBlocking {
        assertTrue(RetrofitCloudApiRepository(null).checkBackendHealth() is CloudApiResult.NotConfigured)
    }

    @Test fun productionClientRequiresHttpsAndTrailingSlash() {
        assertNull(CloudNetworking.service(""))
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            CloudNetworking.service("http://worker.example/")
        }
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            CloudNetworking.service("https://worker.example")
        }
        assertTrue(CloudNetworking.service("https://worker.example/") != null)
    }

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)
}
