package com.serverless.forschungsprojectfaas.model

import android.os.Parcelable
import androidx.recyclerview.widget.DiffUtil
import kotlinx.parcelize.Parcelize

/**
 * This class represents the requirement of NDW.
 * Which caption is represented in which row in which columns
 */
@Parcelize
data class RowEvaluationEntry(
    val row: Int,
    val caption: String,
    val amount: Int,
    val fromColumn: Int,
    val toColumn: Int
): Parcelable {

    val asCsvLine get() = "$row,$caption,$amount,$fromColumn,$toColumn"

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RowEvaluationEntry>(){
            override fun areItemsTheSame(oldItem: RowEvaluationEntry, newItem: RowEvaluationEntry): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: RowEvaluationEntry, newItem: RowEvaluationEntry): Boolean {
                return areItemsTheSame(oldItem, newItem)
            }
        }
    }
}
