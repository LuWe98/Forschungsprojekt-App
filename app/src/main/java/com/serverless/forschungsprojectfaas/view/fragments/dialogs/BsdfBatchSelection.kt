package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.BsdfBatchSelectionBinding
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingBottomSheetDialogFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaBatchSelection
import com.serverless.forschungsprojectfaas.viewmodel.VmBatchSelection
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfBatchSelection: BindingBottomSheetDialogFragment<BsdfBatchSelectionBinding>() {

    private val vm by hiltNavDestinationViewModels<VmBatchSelection>(R.id.bsdfBatchSelection)

    private lateinit var rva: RvaBatchSelection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        rva = RvaBatchSelection().apply {
            onBatchClicked = vm::onBatchClicked
            onLongClicked = vm::onBatchLongClicked
        }

        binding.rv.apply {
            setHasFixedSize(true)
            adapter = rva
            layoutManager = GridLayoutManager(requireContext(), 3)
            disableChangeAnimation()
        }
    }

    private fun initListeners(){
        binding.apply {
            etSearchQuery.onTextChanged(vm::onSearchQueryChanged)
            btnBack.onClick(vm::onBackButtonClicked)
            btnAddBatch.onClick(vm::onAddBatchButtonClicked)
        }
    }

    private fun initObservers(){
        vm.filteredBatchesFlow.collectWhenStarted(viewLifecycleOwner) {
            rva.submitList(it)
        }
    }

}