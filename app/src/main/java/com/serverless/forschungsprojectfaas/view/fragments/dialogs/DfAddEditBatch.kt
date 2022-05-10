package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.DfAddEditBatchBinding
import com.serverless.forschungsprojectfaas.dispatcher.setFragmentResultEventListener
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingDialogFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmAddEditBatch
import com.serverless.forschungsprojectfaas.viewmodel.VmAddEditBatch.*
import com.welu.androidflowutils.collectWhenStarted
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
            btnDelete.isVisible = !vm.isAddMode
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
            btnDelete.onClick(vm::onDeleteBatchButtonClicked)
        }
    }

    private fun initObservers(){
        setFragmentResultEventListener(vm::onColorSelectionResultReceived)

        vm.colorStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.colorCircle.setCardBackgroundColor(it)
        }

        vm.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when(event) {
                is AddEditBatchEvent.ShowMessageSnackBar -> showSnackBar(event.res)
            }
        }
    }
}