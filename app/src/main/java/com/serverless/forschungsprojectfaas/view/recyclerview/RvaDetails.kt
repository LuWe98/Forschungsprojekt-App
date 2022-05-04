package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviEntryMarkBinding
import com.serverless.forschungsprojectfaas.extensions.setBackgroundTint
import com.serverless.forschungsprojectfaas.model.room.junctions.BarBatchWithBars
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaDetails : BindingListAdapter<BarBatchWithBars, RviEntryMarkBinding>(BarBatchWithBars.DIFF_CALLBACK, RviEntryMarkBinding::class) {

    override fun initListeners(binding: RviEntryMarkBinding, vh: BindingListAdapterViewHolder) {

    }

    override fun bindViews(binding: RviEntryMarkBinding, item: BarBatchWithBars, position: Int) {
        binding.apply {
            tvMark.text = item.barBatch.caption
            tvCount.text = item.bars.size.toString()
            tvMark.setBackgroundTint(item.barBatch.colorInt)
        }
    }
}