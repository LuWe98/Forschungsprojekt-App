package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviEntryBinding
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onLongClick
import com.serverless.forschungsprojectfaas.model.room.entities.CapturedPicture
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter
import kotlin.random.Random

class RvaHome : BindingListAdapter<CapturedPicture, RviEntryBinding>(CapturedPicture.DIFF_CALLBACK, RviEntryBinding::class) {

    var onItemClicked : ((CapturedPicture) -> (Unit))? = null

    var onItemLongClicked : ((CapturedPicture) -> (Unit))? = null

    var onMoreOptionsClicked: ((CapturedPicture) -> (Unit))? = null

    override fun initListeners(binding: RviEntryBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClicked?.invoke(getItem(vh)) }
            btnMoreOptions.onClick { onMoreOptionsClicked?.invoke(getItem(vh)) }
            root.onLongClick { onItemLongClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviEntryBinding, item: CapturedPicture, position: Int) {
        binding.apply {
            tvTitle.text = item.title
            tvDateAndQuestionAmount.text = item.timeStampAsDate
            tvMark.text = Random.nextInt(30).toString()
        }
    }

}