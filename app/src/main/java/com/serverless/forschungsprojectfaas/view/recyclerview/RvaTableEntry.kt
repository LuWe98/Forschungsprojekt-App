package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviTableEntryBinding
import com.serverless.forschungsprojectfaas.model.EvaluatedPileResult
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaTableEntry: BindingListAdapter<EvaluatedPileResult, RviTableEntryBinding>(EvaluatedPileResult.DIFF_CALLBACK, RviTableEntryBinding::class) {

    override fun initListeners(binding: RviTableEntryBinding, vh: BindingListAdapterViewHolder) {

    }

    override fun bindViews(binding: RviTableEntryBinding, item: EvaluatedPileResult, position: Int) {
        binding.apply {
            tvRow.text = item.row.toString()
            tvCaption.text = item.caption
            tvAmount.text = item.amount.toString()
            tvFrom.text = item.fromColumn.toString()
            tvTo.text = item.toColumn.toString()
        }
    }
}