package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.DfAddEditBatchBinding
import com.serverless.forschungsprojectfaas.extensions.collectWhenStarted
import com.serverless.forschungsprojectfaas.extensions.hiltNavDestinationViewModels
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onTextChanged
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingDialogFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmAddEditBatch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfAddEditBatch: BindingDialogFragment<DfAddEditBatchBinding>() {

    private val vm by hiltNavDestinationViewModels<VmAddEditBatch>(R.id.dfAddEditBatch)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
        binding.apply {
            editText.setText(vm.caption)
            tvTitle.setText(vm.dialogTitleRes)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnCancel.onClick(vm::onCancelButtonClicked)
            btnConfirm.onClick(vm::onConfirmButtonClicked)
            colorCircle.onClick(vm::onColorBtnClicked)
            editText.onTextChanged(vm::onCaptionTextChanged)
        }
    }

    private fun initObservers(){
        vm.colorStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.colorCircle.setCardBackgroundColor(it)
        }
    }

}