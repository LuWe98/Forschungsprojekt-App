package com.serverless.forschungsprojectfaas.extensions

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

@SuppressLint("DiffUtilEquals")
inline fun <reified T: Any, reified P: Any> generateDiffItemCallback(crossinline idProvider: ((T) -> (P))) = object : DiffUtil.ItemCallback<T>() {
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
    override fun areItemsTheSame(oldItem: T, newItem: T) = idProvider.invoke(oldItem) == idProvider.invoke(newItem)
}