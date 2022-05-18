package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviBatchBinding
import com.serverless.forschungsprojectfaas.extensions.onLongClick
import com.serverless.forschungsprojectfaas.extensions.setBackgroundTint
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.junctions.BatchWithBars
import com.serverless.forschungsprojectfaas.utils.Constants
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaPileBatches : BindingListAdapter<BatchWithBars, RviBatchBinding>(BatchWithBars.DIFF_CALLBACK, RviBatchBinding::class) {

    var onItemLongClicked: ((Batch?) -> (Unit))? = null

    override fun initListeners(binding: RviBatchBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            card.onLongClick {
                onItemLongClicked?.invoke(getItem(vh).batch)
            }
        }
    }

    override fun bindViews(binding: RviBatchBinding, item: BatchWithBars, position: Int) {
        binding.apply {
            tvMark.text = item.batch?.caption ?: Constants.UNASSIGNED_BAR_CAPTION
            tvCount.text = item.bars.size.toString()
            tvMark.setBackgroundTint(item.batch?.colorInt ?: Constants.UNASSIGNED_BAR_COLOR)
        }
    }
}