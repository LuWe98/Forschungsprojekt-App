package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.DfLoadingBinding
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DfLoading: BindingDialogFragment<DfLoadingBinding>() {

    companion object {
        const val LOADING_DIALOG_DISMISS_DELAY = 350L
    }

    private val args: DfLoadingArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.apply {
            isCancelable = false
            setCanceledOnTouchOutside(false)
        }
        initViews()
    }

    private fun initViews(){
        binding.apply {
            tvTitle.setText(args.messageRes)
        }
    }

    override fun getTheme() = R.style.Theme_Forschungsprojekt_DialogFragment_Loading

}