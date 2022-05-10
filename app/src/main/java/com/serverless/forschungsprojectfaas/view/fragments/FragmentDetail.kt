package com.serverless.forschungsprojectfaas.view.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.FragmentDetailBinding
import com.serverless.forschungsprojectfaas.dispatcher.setFragmentResultEventListener
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.custom.subsampling.ImageSource
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaPileBatches
import com.serverless.forschungsprojectfaas.viewmodel.VmDetail
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class FragmentDetail : BindingFragment<FragmentDetailBinding>() {

    companion object {
        private const val MAX_OPACITY = 255
        private const val MAX_BOX_HEIGHT = 150
        private const val MAX_BOX_WIDTH = 150
        private const val SPAN_COUNT = 3
        private const val MAX_ARROW_ROTATION = 180
    }

    private val vm: VmDetail by hiltNavDestinationViewModels(R.id.fragmentDetail)

    private lateinit var rva: RvaPileBatches

    private lateinit var behavior: BottomSheetBehavior<ConstraintLayout>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            etSearchQuery.setText(vm.batchSearchQuery)

            progressWidth.max = MAX_BOX_WIDTH
            progressHeight.max = MAX_BOX_HEIGHT
            progressOpacity.max = MAX_OPACITY

            progressOpacity.progress = vm.barOpacity
            iv.setBoxAlpha(vm.barOpacity)
            progressStroke.progress = vm.barStroke
            iv.setBoxStroke(vm.barStroke)
        }

        rva = RvaPileBatches().apply {

        }

        binding.rv.apply {
            adapter = rva
            layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
            disableChangeAnimation()
            setHasFixedSize(true)
        }

        behavior = BottomSheetBehavior.from(binding.sheetLayout).apply {
            peekHeight = 70.dp
        }
    }

    private fun initListeners() {
        binding.apply {
            iv.apply {
                onCanvasClicked = vm::onImageClicked
                onCanvasLongClicked = vm::onImageLongClicked
                isBarSelected = vm::isBarSelected
                onBoxDragReleased = vm::onBarDragReleased
            }

            btnBack.onClick(vm::onBackButtonClicked)
            btnConnect.onClick(vm::onSelectBarsInLineClicked)
            btnClearSelection.onClick(vm::onClearSelectionClicked)
            btnSwap.onClick(vm::onSwapBatchOfSelectedBarsClicked)
            btnDelete.onClick(vm::onDeleteSelectedBarsClicked)

            progressOpacity.onProgressChanged { progress, _ ->
                vm.onOpacityProgressChanged(progress)
                iv.setBoxAlpha(progress)
            }
            progressStroke.onProgressChanged { progress, _ ->
                vm.onStrokeWidthProgressChanged(progress)
                iv.setBoxStroke(progress)
            }

            progressWidth.onProgressChanged { progress, isUserInput ->
                vm.onWidthProgressChanged(progress, isUserInput)
            }

            progressHeight.onProgressChanged { progress, isUserInput ->
                vm.onHeightProgressChanged(progress, isUserInput)
            }

            btnExpandSheet.onClick(::toggleSheet)

            etSearchQuery.onTextChanged(vm::onBatchSearchQueryChanged)

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.btnExpandSheet.rotation = slideOffset * MAX_ARROW_ROTATION
                }
            })
        }
    }

    private fun toggleSheet() {
        if (behavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else if (behavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun initObservers() {
        setFragmentResultEventListener(vm::onBatchSelectionResultReceived)

        vm.pileTitleFlow.collectWhenStarted(viewLifecycleOwner) { title ->
            binding.pageTitle.text = title
        }

        vm.imageBitmapStateFlow.collectWhenStarted(viewLifecycleOwner) { bitmap ->
            binding.progress.isVisible = false
            bitmap?.let {
                binding.iv.apply {
                    setImage(ImageSource.bitmap(it.copy(it.config, false)), state)
                }
            }
        }

        vm.barBatchWithBarsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            binding.iv.setBoxes(it)
        }

        vm.filteredBatchWithBarsFlow.collectWhenStarted(viewLifecycleOwner) {
            rva.submitList(it)
        }

        vm.allBarsFlow.collectWhenStarted(viewLifecycleOwner) { bars ->
            binding.tvSticksAmount.text = if (bars.isEmpty()) "-" else bars.size.toString()
        }

        vm.selectedBarIdsStateFlow.collectWhenStarted(viewLifecycleOwner) { selectedIds ->
            binding.apply {
                btnClearSelection.isEnabled = selectedIds.isNotEmpty()
                btnSwap.isEnabled = selectedIds.isNotEmpty()
                btnDelete.isEnabled = selectedIds.isNotEmpty()
                btnConnect.isEnabled = selectedIds.size == 2
                iv.invalidate()
            }
        }

        vm.selectedBarsStateFlow.collectWhenStarted(viewLifecycleOwner) { bars ->
            binding.apply {
                progressWidth.isVisible = bars.size == 1
                tvWidthTitle.isVisible = bars.size == 1
                progressHeight.isVisible = bars.size == 1
                tvHeightTitle.isVisible = bars.size == 1
            }

            if(bars.size == 1){
                binding.apply {
                    progressWidth.progress = bars.first().rect.width().toInt()
                    progressHeight.progress = bars.first().rect.height().toInt()
                }
            }
        }
    }
}