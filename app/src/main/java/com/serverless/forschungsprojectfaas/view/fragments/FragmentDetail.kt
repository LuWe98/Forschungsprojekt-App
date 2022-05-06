package com.serverless.forschungsprojectfaas.view.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.davemorrissey.labs.subscaleview.ImageSource
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.FragmentDetailBinding
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaDetails
import com.serverless.forschungsprojectfaas.viewmodel.VmDetail
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class FragmentDetail : BindingFragment<FragmentDetailBinding>() {

    private val vm: VmDetail by hiltNavDestinationViewModels(R.id.fragmentDetail)

    private lateinit var rva: RvaDetails

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        rva = RvaDetails()

        binding.rv.apply {
            adapter = rva
            layoutManager = LinearLayoutManager(requireContext())
            disableChangeAnimation()
            setHasFixedSize(true)
        }
    }

    private fun initListeners() {
        binding.apply {
            btnBack.onClick(vm::onBackButtonClicked)
            btnBack.onLongClick {
                vm.onGoToBatchSelectionClicked()
            }
        }
    }

    private fun initObservers() {
        vm.entryTitleFlow.collectWhenStarted(viewLifecycleOwner) { title ->
            binding.pageTitle.text = title
        }

        vm.imageBitmapStateFlow.collectWhenStarted(viewLifecycleOwner) { bitmap ->
            binding.progress.isVisible = false
            binding.iv.setImage(ImageSource.bitmap(bitmap))
        }

        vm.barBatchWithBarsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rva.submitList(it.sortedBy { it.batch.caption })
            binding.iv.setRectangles(it)
        }

        vm.allBarsFlow.collectWhenStarted(viewLifecycleOwner) { bars ->
            binding.tvSticksAmount.text = if (bars.isEmpty()) "-" else bars.size.toString()
        }
    }
}