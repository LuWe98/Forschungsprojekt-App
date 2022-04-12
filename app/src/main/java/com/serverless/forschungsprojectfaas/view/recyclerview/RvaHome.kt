package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviEntryBinding
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onLongClick
import com.serverless.forschungsprojectfaas.model.room.entities.PictureEntry
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter
import kotlin.random.Random

class RvaHome : BindingListAdapter<PictureEntry, RviEntryBinding>(PictureEntry.DIFF_CALLBACK, RviEntryBinding::class) {

    var onItemClicked : ((PictureEntry) -> (Unit))? = null

    var onItemLongClicked : ((PictureEntry) -> (Unit))? = null

    var onMoreOptionsClicked: ((PictureEntry) -> (Unit))? = null

    override fun initListeners(binding: RviEntryBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClicked?.invoke(getItem(vh)) }
            btnMoreOptions.onClick { onMoreOptionsClicked?.invoke(getItem(vh)) }
            root.onLongClick { onItemLongClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviEntryBinding, item: PictureEntry, position: Int) {
        log("URI: ${item.pictureUri}")
        binding.apply {
            tvTitle.text = item.name
            tvDateAndQuestionAmount.text = item.timeStampAsDate
            tvMark.text = Random.nextInt(30).toString()
        }
    }

}