package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviEntryBatchBinding
import com.serverless.forschungsprojectfaas.extensions.setBackgroundTint
import com.serverless.forschungsprojectfaas.model.room.junctions.BatchWithBars
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaDetails : BindingListAdapter<BatchWithBars, RviEntryBatchBinding>(BatchWithBars.DIFF_CALLBACK, RviEntryBatchBinding::class) {

    override fun initListeners(binding: RviEntryBatchBinding, vh: BindingListAdapterViewHolder) {

    }

    override fun bindViews(binding: RviEntryBatchBinding, item: BatchWithBars, position: Int) {
        binding.apply {
            tvMark.text = item.batch.caption
            tvCount.text = item.bars.size.toString()
            tvMark.setBackgroundTint(item.batch.colorInt)
        }
    }
}