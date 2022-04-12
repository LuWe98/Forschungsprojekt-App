package com.serverless.forschungsprojectfaas.extensions

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

inline fun <reified T, reified P> generateDiffItemCallback(crossinline idProvider: ((T) -> (P))) = object : DiffUtil.ItemCallback<T>() {
    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
    override fun areItemsTheSame(oldItem: T, newItem: T) = idProvider.invoke(oldItem) == idProvider.invoke(newItem)
}