package com.serverless.forschungsprojectfaas.view.recyclerview

import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.RviMenuBinding
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionTypeItemMarker
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onLongClick
import com.serverless.forschungsprojectfaas.extensions.setImageDrawable
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter

class RvaSelectionDialog : BindingListAdapter<SelectionTypeItemMarker<*>, RviMenuBinding>(SelectionTypeItemMarker.DIFF_CALLBACK, RviMenuBinding::class) {

    var onItemClicked: ((SelectionTypeItemMarker<*>) -> (Unit))? = null

    var selectionPredicate: ((SelectionTypeItemMarker<*>) -> (Boolean)) = { false }

    var selectionColor: Int? = null

    override fun initListeners(binding: RviMenuBinding, vh: BindingListAdapterViewHolder) {
        binding.root.apply {
            onClick { onItemClicked?.invoke(getItem(vh)) }
            onLongClick { onItemClicked?.invoke(getItem(vh)) }
        }
    }

    override fun bindViews(binding: RviMenuBinding, item: SelectionTypeItemMarker<*>, position: Int) {
        binding.apply {
            title.text = root.context.getString(item.textRes)
            icon.setImageDrawable(item.iconRes)

            if(selectionPredicate.invoke(item) && selectionColor != null){
                root.setCardBackgroundColor(selectionColor!!)
            } else {
                root.setCardBackgroundColor(root.context.getColor(R.color.transparent))
            }
        }
    }
}