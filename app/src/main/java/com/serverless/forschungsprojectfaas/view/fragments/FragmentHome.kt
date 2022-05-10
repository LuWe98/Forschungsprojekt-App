package com.serverless.forschungsprojectfaas.view.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.serverless.forschungsprojectfaas.databinding.FragmentHomeBinding
import com.serverless.forschungsprojectfaas.dispatcher.setFragmentResultEventListener
import com.serverless.forschungsprojectfaas.extensions.disableChangeAnimation
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onTextChanged
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaHome
import com.serverless.forschungsprojectfaas.viewmodel.VmHome
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentHome : BindingFragment<FragmentHomeBinding>() {

    private val vm : VmHome by viewModels()
    private lateinit var rva: RvaHome

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.etSearchQuery.setText(vm.searchQuery)

        rva = RvaHome().apply {
            onItemClicked = vm::onRvaItemClicked
            onMoreOptionsClicked = vm::onRvaItemMoreOptionsClicked
            onItemLongClicked = vm::onRvaItemMoreOptionsClicked
        }

        binding.rv.apply {
            adapter = rva
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            disableChangeAnimation()
        }
    }

    private fun initListeners() {
        setFragmentResultEventListener(vm::onPictureMoreOptionsResultReceived)
        setFragmentResultEventListener(vm::onOrderBySelectionResultReceived)

        binding.apply {
            fab.onClick(vm::onFabClicked)
            btnFilter.onClick(vm::onSortButtonClicked)
            etSearchQuery.onTextChanged(vm::onSearchQueryChanged)
        }
    }

    private fun initObservers() {
        vm.pilesWithBarCount.collectWhenStarted(viewLifecycleOwner) {
            rva.submitList(it)
        }
    }

}