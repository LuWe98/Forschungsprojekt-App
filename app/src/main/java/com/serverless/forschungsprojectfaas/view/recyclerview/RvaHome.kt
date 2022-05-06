package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviEntryBinding
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onLongClick
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter
import kotlin.random.Random

class RvaHome : BindingListAdapter<Pile, RviEntryBinding>(Pile.DIFF_CALLBACK, RviEntryBinding::class) {

    var onItemClicked : ((Pile) -> (Unit))? = null

    var onItemLongClicked : ((Pile) -> (Unit))? = null

    var onMoreOptionsClicked: ((Pile) -> (Unit))? = null

    override fun initListeners(binding: RviEntryBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClicked?.invoke(getItem(vh)) }
            btnMoreOptions.onClick { onMoreOptionsClicked?.invoke(getItem(vh)) }
            root.onLongClick { onItemLongClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviEntryBinding, item: Pile, position: Int) {
        binding.apply {
            tvTitle.text = item.title
            tvDateAndQuestionAmount.text = item.timeStampAsDate
            tvMark.text = Random.nextInt(30).toString()
        }
    }

}