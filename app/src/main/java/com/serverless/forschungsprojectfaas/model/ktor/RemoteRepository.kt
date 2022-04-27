package com.serverless.forschungsprojectfaas.model.ktor

import io.ktor.client.*
import io.ktor.client.request.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteRepository @Inject constructor(
    private val client: HttpClient
) {

    companion object {
        private const val PORT = 8080
        const val REMOTE_URL = "http://141.28.73.147:$PORT/function"
    }

    suspend fun invokeTestFunction(parameter: String) = client.post("testfunction") {
        setBody(parameter)
    }

}