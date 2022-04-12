package com.serverless.forschungsprojectfaas.extensions

import android.util.Log

enum class LogType {
    Debug,
    Error,
    Wtf
}

fun Any.log(text: String, logType: LogType = LogType.Debug) = when (logType) {
    LogType.Debug -> Log.d(tag, text)
    LogType.Error -> Log.e(tag, text)
    LogType.Wtf -> Log.wtf(tag, text)
}

private val tag get() = "ManualLog"