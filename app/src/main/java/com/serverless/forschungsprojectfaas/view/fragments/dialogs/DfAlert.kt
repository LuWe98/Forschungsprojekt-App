package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.DfAlertBinding
import com.serverless.forschungsprojectfaas.extensions.hiltNavDestinationViewModels
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingDialogFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmAlert
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfAlert: BindingDialogFragment<DfAlertBinding>() {

    private val vm by hiltNavDestinationViewModels<VmAlert>(R.id.dfAlert)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initListeners()
    }

    private fun initViews(){
        binding.apply {
            tvTitle.setText(vm.parsedMessage.titleRes)
            tvText.setText(vm.parsedMessage.textRes)
        }
    }

    private fun initListeners(){
        binding.apply {
            btnConfirm.onClick(vm::onConfirmButtonClicked)
        }
    }

    enum class AlertMessage(
        @StringRes val titleRes: Int,
        @StringRes val textRes: Int
    ){
        EXPORT_CSV_FILE_CREATION_ERROR(
            titleRes = R.string.errorCsvFileAlreadyPresentTitle,
            textRes = R.string.errorCsvFileAlreadyPresentText
        ),
        ADD_EDIT_BATCH_CAPTION_IS_EMPTY(
            titleRes = R.string.batchSaveError,
            textRes = R.string.errorCaptionCannotBeEmpty
        ),
        ADD_EDIT_BATCH_CAPTION_ALREADY_USED(
            titleRes = R.string.batchSaveError,
            textRes = R.string.errorCaptionAlreadyUsed
        )
    }
}