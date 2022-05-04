package com.serverless.forschungsprojectfaas.extensions

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil
import kotlin.reflect.KProperty1

@SuppressLint("DiffUtilEquals")
inline fun <reified T: Any, reified P: Any> generateDiffItemCallback(crossinline idProvider: ((T) -> (P))) = object : DiffUtil.ItemCallback<T>() {
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
    override fun areItemsTheSame(oldItem: T, newItem: T) = idProvider.invoke(oldItem) == idProvider.invoke(newItem)
}

inline operator fun <reified A, reified B, reified C> KProperty1<A, B>.div(crossinline getter: (B) -> C): (A) -> C = {
    getter(this(it))
}