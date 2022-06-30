package com.serverless.forschungsprojectfaas.view.recyclerview

import androidx.core.view.isVisible
import com.serverless.forschungsprojectfaas.databinding.RviPileBinding
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onLongClick
import com.serverless.forschungsprojectfaas.extensions.setImageDrawable
import com.serverless.forschungsprojectfaas.model.PileStatus.*
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBarCount
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaHome : BindingListAdapter<PileWithBarCount, RviPileBinding>(PileWithBarCount.DIFF_CALLBACK, RviPileBinding::class) {

    var onItemClicked : ((Pile) -> (Unit))? = null

    var onItemLongClicked : ((Pile) -> (Unit))? = null

    var onMoreOptionsClicked: ((Pile) -> (Unit))? = null

    var onStatusButtonClicked: ((Pile) -> (Unit))? = null

    override fun initListeners(binding: RviPileBinding, vh: BindingListAdapterViewHolder) {
        binding.apply {
            root.onClick {
                onItemClicked?.invoke( getItem(vh).pile)
            }
            root.onLongClick {
                onItemLongClicked?.invoke( getItem(vh).pile)
            }
            btnMoreOptions.onClick { onMoreOptionsClicked?.invoke(getItem(vh).pile) }
            btnStatus.onClick { onStatusButtonClicked?.invoke(getItem(vh).pile) }
        }
    }

    override fun bindViews(binding: RviPileBinding, item: PileWithBarCount, position: Int) {
        binding.apply {
            tvTitle.text = item.pile.title
            tvDateAndQuestionAmount.text = item.pile.timeStampAsDateString

            if(item.pile.pileStatus == EVALUATING){
                tvMark.text = ""
                btnStatus.isVisible = false
                root.isEnabled = false
                btnMoreOptions.isEnabled = false
                progress.isVisible = true
            } else {
                tvMark.text = if(item.count == 0 || item.count == 1) "-" else item.count.toString()
                btnStatus.isVisible = true
                btnMoreOptions.isEnabled = true
                progress.isVisible = false
                root.isEnabled = true
            }

            btnStatus.setImageDrawable(item.pile.pileStatus.iconRes)
        }
    }

}