package com.serverless.forschungsprojectfaas.dispatcher.base

import com.serverless.forschungsprojectfaas.view.ActivityMain

interface DispatchEvent {

    suspend fun execute(activity: ActivityMain)

}