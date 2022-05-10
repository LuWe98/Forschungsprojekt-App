package com.serverless.forschungsprojectfaas.extensions

val String.isValidHex get(): Boolean = matches(Regex("^#?([A-Fa-f0-9]{6}$|[A-Fa-f0-9]{8}$)"))