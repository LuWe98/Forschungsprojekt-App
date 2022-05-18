package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.BsdfRowMappingResultsBinding
import com.serverless.forschungsprojectfaas.extensions.disableChangeAnimation
import com.serverless.forschungsprojectfaas.extensions.hiltNavDestinationViewModels
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingBottomSheetDialogFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaTableEntry
import com.serverless.forschungsprojectfaas.viewmodel.VmDetail
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

//TODO -> Das hier noch imlementieren. Ist die zweite Seite des Detail ViewPagers
@AndroidEntryPoint
class BsdfRowMappingResults : BindingBottomSheetDialogFragment<BsdfRowMappingResultsBinding>() {

    private val vm by hiltNavDestinationViewModels<VmDetail>(R.id.fragmentDetail)

    private lateinit var rva: RvaTableEntry

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        rva = RvaTableEntry().apply {

        }

        binding.rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rva
            setHasFixedSize(true)
            disableChangeAnimation()
        }
    }

    private fun initListeners() {

    }

    private fun initObservers() {
        vm.evaluatedBarResultsStateFlow.collectWhenStarted(viewLifecycleOwner) { evaluatedBars ->
            rva.submitList(evaluatedBars)
        }
    }
}