package com.serverless.forschungsprojectfaas.dispatcher

import com.serverless.forschungsprojectfaas.dispatcher.base.DispatchEvent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@ActivityRetainedScoped
class DispatchEventPublisher {

    private val eventChannel = Channel<DispatchEvent>()

    val eventChannelFlow = eventChannel.receiveAsFlow()

    suspend fun dispatchToQueue(event: DispatchEvent) = eventChannel.send(event)

}