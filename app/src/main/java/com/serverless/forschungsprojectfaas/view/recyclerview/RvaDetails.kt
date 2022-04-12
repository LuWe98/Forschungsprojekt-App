package com.serverless.forschungsprojectfaas.view.recyclerview

import android.graphics.Color
import com.serverless.forschungsprojectfaas.databinding.RviEntryMarkBinding
import com.serverless.forschungsprojectfaas.extensions.setBackgroundTint
import com.serverless.forschungsprojectfaas.model.room.entities.StickEntry
import com.serverless.forschungsprojectfaas.view.recyclerview.generic.BindingListAdapter
import kotlin.random.Random

class RvaDetails : BindingListAdapter<StickEntry, RviEntryMarkBinding>(StickEntry.DIFF_CALLBACK, RviEntryMarkBinding::class) {


    override fun initListeners(binding: RviEntryMarkBinding, vh: BindingListAdapterViewHolder) {

    }

    override fun bindViews(binding: RviEntryMarkBinding, item: StickEntry, position: Int) {
        binding.apply {
            tvMark.text = item.designation
            tvCount.text = position.toString()
            val color = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
            tvMark.setBackgroundTint(color)
        }
    }

}