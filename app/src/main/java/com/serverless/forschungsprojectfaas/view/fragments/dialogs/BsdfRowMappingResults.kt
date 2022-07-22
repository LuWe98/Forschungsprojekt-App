package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.BsdfPileEvaluationBinding
import com.serverless.forschungsprojectfaas.extensions.disableChangeAnimation
import com.serverless.forschungsprojectfaas.extensions.hiltNavDestinationViewModels
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingBottomSheetDialogFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaEvaluatedRowEntries
import com.serverless.forschungsprojectfaas.viewmodel.VmDetail
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfRowMappingResults : BindingBottomSheetDialogFragment<BsdfPileEvaluationBinding>() {

    private val vm by hiltNavDestinationViewModels<VmDetail>(R.id.fragmentDetail)

    private lateinit var rva: RvaEvaluatedRowEntries

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        rva = RvaEvaluatedRowEntries()

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rva
            setHasFixedSize(true)
            disableChangeAnimation()
        }
    }

    private fun initListeners() {
        binding.apply {
            btnBack.onClick(vm::onPileEvaluationDialogBackPressed)
            btnExport.onClick(vm::onPileEvaluationExportButtonClicked)
        }
    }

    private fun initObservers() {
        vm.evaluatedRowEntriesStateFlow.collectWhenStarted(viewLifecycleOwner) { evaluatedRowEntries ->
            rva.submitList(evaluatedRowEntries)
        }
    }
}