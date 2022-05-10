package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.databinding.RviPileBinding
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onLongClick
import com.serverless.forschungsprojectfaas.extensions.setImageDrawable
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBarCount
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaHome : BindingListAdapter<PileWithBarCount, RviPileBinding>(PileWithBarCount.DIFF_CALLBACK, RviPileBinding::class) {

    var onItemClicked : ((Pile) -> (Unit))? = null

    var onItemLongClicked : ((Pile) -> (Unit))? = null

    var onMoreOptionsClicked: ((Pile) -> (Unit))? = null

    override fun initListeners(binding: RviPileBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick { onItemClicked?.invoke(getItem(vh).pile) }
            btnMoreOptions.onClick { onMoreOptionsClicked?.invoke(getItem(vh).pile) }
            root.onLongClick { onItemLongClicked?.invoke(getItem(vh).pile) }
        }
    }

    override fun bindViews(binding: RviPileBinding, item: PileWithBarCount, position: Int) {
        binding.apply {
            tvTitle.text = item.pile.title
            tvDateAndQuestionAmount.text = item.pile.timeStampAsDate
            tvMark.text = item.count.toString()
            btnStatus.setImageDrawable(item.pile.pileStatus.iconRes)
        }
    }
}