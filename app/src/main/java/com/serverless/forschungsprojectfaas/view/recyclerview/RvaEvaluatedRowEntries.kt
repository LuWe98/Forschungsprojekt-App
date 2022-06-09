package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviTableEntryBinding
import com.serverless.forschungsprojectfaas.model.RowEvaluationEntry
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaEvaluatedRowEntries: BindingListAdapter<RowEvaluationEntry, RviTableEntryBinding>(RowEvaluationEntry.DIFF_CALLBACK, RviTableEntryBinding::class) {

    override fun initListeners(binding: RviTableEntryBinding, vh: BindingListAdapterViewHolder) {

    }

    override fun bindViews(binding: RviTableEntryBinding, item: RowEvaluationEntry, position: Int) {
        binding.apply {
            tvRow.text = item.row.toString()
            tvCaption.text = item.caption
            tvAmount.text = item.amount.toString()
            tvFrom.text = item.fromColumn.toString()
            tvTo.text = item.toColumn.toString()
        }
    }
}