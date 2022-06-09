package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.DfAddEditBatchBinding
import com.serverless.forschungsprojectfaas.dispatcher.setFragmentResultEventListener
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingDialogFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmAddEditBatch
import com.serverless.forschungsprojectfaas.viewmodel.VmAddEditBatch.*
import com.welu.androidflowutils.collectWhenStarted
import com.welu.androidflowutils.launch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import java.util.*

@AndroidEntryPoint
class DfAddEditBatch : BindingDialogFragment<DfAddEditBatchBinding>() {

    private val vm by hiltNavDestinationViewModels<VmAddEditBatch>(R.id.dfAddEditBatch)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            btnDelete.isVisible = !vm.isAddMode
            etBatchSecond.setText(vm.secondBatchLetter)
            etBatchFirst.setText(vm.firstBatchLetter)
            tvTitle.setText(vm.dialogTitleRes)
        }
    }

    private fun initListeners() {
        binding.apply {
            btnCancel.onClick(vm::onCancelButtonClicked)
            btnConfirm.onClick(vm::onConfirmButtonClicked)
            colorCircle.onClick(vm::onColorBtnClicked)
            btnDelete.onClick(vm::onDeleteBatchButtonClicked)
        }

        binding.apply {
            etBatchFirst.onTextChanged { newText, textBefore ->
                vm.convertToValidText(newText.trim(), textBefore).let { adjustedText ->
                    if (!newText.contentEquals(adjustedText)) {
                        etBatchFirst.setText(adjustedText)
                        return@let
                    }
                    etBatchFirst.setSelection(adjustedText.length)

                    if (adjustedText.isNotBlank() && etBatchSecond.text?.isBlank() == true) {
                        etBatchSecond.requestFocus()
                    }
                    vm.onFirstBatchLetterChanged(adjustedText)
                }
            }

            etBatchSecond.onTextChanged { newText, textBefore ->
                vm.convertToValidText(newText.trim(), textBefore).let { adjustedText ->
                    if (!newText.contentEquals(adjustedText)) {
                        etBatchSecond.setText(adjustedText)
                        return@let
                    }
                    etBatchSecond.setSelection(adjustedText.length)
                    vm.onSecondBatchLetterChanged(adjustedText)
                }
            }
        }
    }

    private fun initObservers() {
        setFragmentResultEventListener(vm::onColorSelectionResultReceived)

        vm.colorStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.colorCircle.setCardBackgroundColor(it)
        }

        vm.eventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is AddEditBatchEvent.ShowMessageSnackBar -> showSnackBar(event.res)
            }
        }
    }
}