package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.DfExportCsvFileBinding
import com.serverless.forschungsprojectfaas.extensions.hiltNavDestinationViewModels
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onTextChanged
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingDialogFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmExportPileEvaluationResults
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfExportPileEvaluationResults: BindingDialogFragment<DfExportCsvFileBinding>() {

    private val vm by hiltNavDestinationViewModels<VmExportPileEvaluationResults>(R.id.dfExportBarEvaluationResults)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            etFileName.setText(vm.fileName)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnCancel.onClick(vm::onCancelButtonClicked)
            btnConfirm.onClick(vm::onConfirmButtonClicked)
            includeHeadersLayout.onClick(vm::onIncludeHeaderLayoutClicked)
            etFileName.onTextChanged(vm::onFileNameTextChanged)
        }
    }

    private fun initObservers(){
        vm.includeHeaderStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.includeHeadersCheckbox.isChecked = it
        }
    }
}