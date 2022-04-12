package com.serverless.forschungsprojectfaas.view.recyclerview.generic

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingHelper
import kotlin.reflect.KClass

abstract class BindingListAdapter<T : Any, B : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val clazz: KClass<B>
) : ListAdapter<T, BindingListAdapter<T, B>.BindingListAdapterViewHolder>(diffCallback) {

    override fun onBindViewHolder(vh: BindingListAdapterViewHolder, position: Int) {
        getItem(position)?.let { vh.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BindingListAdapterViewHolder(BindingHelper.getViewHolderBinding(clazz, parent))

    inner class BindingListAdapterViewHolder(private val binding: B) : BindingViewHolder<T>(binding) {
        init {
            initListeners(binding, this)
        }

        override fun bind(item: T) {
            bindViews(binding, item, adapterPosition)
        }
    }

    fun getItem(viewHolder: RecyclerView.ViewHolder): T = getItem(viewHolder.adapterPosition)

    abstract fun initListeners(binding: B, vh: BindingListAdapterViewHolder)

    abstract fun bindViews(binding: B, item: T, position: Int)

    fun moveItem(fromPosition : Int, toPosition : Int){
        submitList(currentList.toMutableList().apply {
            add(toPosition, removeAt(fromPosition))
        })
    }
}