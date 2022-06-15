package com.serverless.forschungsprojectfaas.extensions

fun <T> MutableList<T>.setItems(items: List<T>): List<T> {
    clear()
    addAll(items)
    return this
}

fun <T> MutableList<T>.addAllChain(items: List<T>): MutableList<T> = apply { addAll(items) }
