package com.serverless.forschungsprojectfaas.dispatcher.base

interface Dispatcher <T : DispatchEvent> {
    suspend fun dispatch(event: T)
}