package com.serverless.forschungsprojectfaas.view.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.FragmentDetailBinding
import com.serverless.forschungsprojectfaas.dispatcher.setFragmentResultEventListener
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaPileBatches
import com.serverless.forschungsprojectfaas.viewmodel.VmDetail
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

//TODO -> Liste reverten, auf der hauptseite sollen nur die Bars agenezigt werden. -> Die Liste wird in einem separaten Fenster angezeigt mit button press
@RequiresApi(Build.VERSION_CODES.N)
@AndroidEntryPoint
class FragmentDetail : BindingFragment<FragmentDetailBinding>() {

    companion object {
        private const val MAX_OPACITY = 255
        private const val MAX_BOX_HEIGHT = 150
        private const val MAX_BOX_WIDTH = 150
        private const val SPAN_COUNT = 3
        private const val MAX_ARROW_ROTATION = 180

        private const val MIN_VALUE = 50
        private const val WHITE_COLOR_FULL = 255 - MIN_VALUE
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
            onItemLongClicked = vm::onBatchLongClicked
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

            btnClearSelectedBars.onClick(vm::onClearSelectionClicked)
            btnQuickSelectFirstBatch.onClick(vm::onQuickChangeBatchButtonClicked)

            btnSwap.onClick(vm::onSwapBatchOfSelectedBarsClicked)
            btnDelete.onClick(vm::onDeleteSelectedBarsClicked)
            btnShowRowMappingScreen.onClick(vm::onShowRowMappingDialogClicked)

            progressOpacity.onProgressChanged { progress, _ ->
                vm.onOpacityProgressChanged(progress)
                iv.setBoxAlpha(progress)
            }
            progressStroke.onProgressChanged { progress, _ ->
                vm.onStrokeWidthProgressChanged(progress)
                iv.setBoxStroke(progress)
            }

            progressWidth.onProgressChanged(vm::onWidthProgressChanged)
            progressHeight.onProgressChanged(vm::onHeightProgressChanged)
            etSearchQuery.onTextChanged(vm::onBatchSearchQueryChanged)
            btnExpandSheet.onClick(::toggleSheet)

            behavior.addBottomSheetCallback(bottomSheetBehaviour)
        }
    }

    private val bottomSheetBehaviour = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) = Unit
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            binding.btnExpandSheet.rotation = slideOffset * MAX_ARROW_ROTATION
//                    val color = (WHITE_COLOR_FULL * slideOffset).toInt() + MIN_VALUE
//                    requireActivity().window.apply {
//                        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                        statusBarColor = color
//                    }
//                    binding.sheetHeader.setBackgroundColor(Color.rgb(color, color, color))

            //binding.sheetBackgroundDim.alpha = (1 - slideOffset).pow(10)
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

        vm.imageBitmapStateFlow.collectWhenStarted(viewLifecycleOwner) { bitmap ->
            bitmap?.let {
                binding.iv.setImage(ImageSource.bitmap(it.copy(it.config, false)), binding.iv.state).apply {
                    binding.progress.isVisible = false
                }
            }
        }

        vm.pileTitleFlow.collectWhenStarted(viewLifecycleOwner, collector = binding.pageTitle::setText)

        vm.barBatchWithBarsStateFlow.collectWhenStarted(viewLifecycleOwner, collector = binding.iv::setBoxes)

        vm.filteredBatchWithBarsFlow.collectWhenStarted(viewLifecycleOwner,250,  rva::submitList)

        vm.allBarsFlow.collectWhenStarted(viewLifecycleOwner) { bars ->
            binding.tvSticksAmount.text = if (bars.isEmpty()) "-" else bars.size.toString()
        }

        vm.batchOfFirstSelectedBarStateFlow.collectWhenStarted(viewLifecycleOwner) { batch ->
            binding.apply {
                ivSelectedBatchLabel.isEnabled = batch != null
                tvSelectedBatchLabel.isEnabled = batch != null
                btnQuickSelectFirstBatch.isEnabled = batch != null
                ivSelectedBatchLabel.setImageDrawable(if(batch == null) R.drawable.ic_label_outlined else R.drawable.ic_label)
                tvSelectedBatchLabel.text = batch?.caption ?: "-"
            }
        }

        vm.selectedBarIdsStateFlow.collectWhenStarted(viewLifecycleOwner) { selectedIds ->
            binding.apply {
                tvSelectedNumber.text = selectedIds.size.toString()
                ivSelectedBarsIcon.setImageDrawable(if (selectedIds.isEmpty()) R.drawable.ic_grid_empty else R.drawable.ic_grid_filled)

                selectedIds.isNotEmpty().let { isNotEmpty ->
                    tvSelectedNumber.isEnabled = isNotEmpty
                    ivSelectedBarsIcon.isEnabled = isNotEmpty
                    btnSwap.isEnabled = isNotEmpty
                    btnDelete.isEnabled = isNotEmpty
                    btnClearSelectedBars.isEnabled = isNotEmpty
                }

                btnConnect.isEnabled = vm.areBarsInSameRow(selectedIds)
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

            if (bars.size == 1) {
                binding.apply {
                    progressWidth.progress = bars.first().rect.width().toInt()
                    progressHeight.progress = bars.first().rect.height().toInt()
                }
            }
        }
    }

    override fun onDestroyView() {
        behavior.removeBottomSheetCallback(bottomSheetBehaviour)
        super.onDestroyView()
    }
}