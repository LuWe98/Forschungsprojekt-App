package com.serverless.forschungsprojectfaas.model

import androidx.recyclerview.widget.DiffUtil

data class EvaluatedPileResult(
    val row: Int,
    val caption: String,
    val amount: Int,
    val fromColumn: Int,
    val toColumn: Int
) {
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<EvaluatedPileResult>(){
            override fun areItemsTheSame(oldItem: EvaluatedPileResult, newItem: EvaluatedPileResult): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: EvaluatedPileResult, newItem: EvaluatedPileResult): Boolean {
                return areItemsTheSame(oldItem, newItem)
            }
        }
    }
}
