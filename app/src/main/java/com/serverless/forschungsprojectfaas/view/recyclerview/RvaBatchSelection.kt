package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviBatchBrowseBinding
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onLongClick
import com.serverless.forschungsprojectfaas.extensions.setBackgroundTint
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaBatchSelection: BindingListAdapter<Batch, RviBatchBrowseBinding>(Batch.DIFF_CALLBACK, RviBatchBrowseBinding::class) {

    var onBatchClicked: ((Batch) -> (Unit))? = null

    var onMoreOptionsClicked: ((Batch) -> (Unit))? = null

    var onLongClicked: ((Batch) -> (Unit))? = null

    override fun initListeners(binding: RviBatchBrowseBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            card.onClick {
                onBatchClicked?.invoke(getItem(vh))
            }

            card.onLongClick {
                onLongClicked?.invoke(getItem(vh))
            }
        }
    }

    override fun bindViews(binding: RviBatchBrowseBinding, item: Batch, position: Int) {
        binding.apply {
            tvMark.text = item.caption
            tvMark.setBackgroundTint(item.colorInt)
        }
    }
}