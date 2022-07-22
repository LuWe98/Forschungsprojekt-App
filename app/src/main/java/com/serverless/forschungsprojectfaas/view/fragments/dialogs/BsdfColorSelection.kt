package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.view.drawToBitmap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.BsdfColorSelectionBinding
import com.serverless.forschungsprojectfaas.extensions.hiltNavDestinationViewModels
import com.serverless.forschungsprojectfaas.extensions.onClick
import com.serverless.forschungsprojectfaas.extensions.onProgressChanged
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingBottomSheetDialogFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmColorSelection
import com.serverless.forschungsprojectfaas.viewmodel.VmColorSelection.ColorType
import com.welu.androidflowutils.collectWhenStarted

class BsdfColorSelection : BindingBottomSheetDialogFragment<BsdfColorSelectionBinding>() {

    private val vm by hiltNavDestinationViewModels<VmColorSelection>(R.id.bsdfColorSelection)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        enableFullscreenMode()

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            progressRed.max = 255
            progressGreen.max = 255
            progressBlue.max = 255
            progressAlpha.max = 255
            oldColorCard.setCardBackgroundColor(vm.currentColorValue)
            oldColorText.text = Integer.toHexString(vm.currentColorValue)
        }

        addBottomSheetStateChangedAction { _, state ->
            if(state == BottomSheetBehavior.STATE_DRAGGING) {
                behaviour?.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListeners() {
        binding.apply {
            btnBack.onClick(vm::onCancelButtonClicked)
            btnSave.onClick(vm::onSaveButtonClicked)

            progressRed.onProgressChanged { progress, fromUser ->
                vm.onSeekBarChanged(progress, fromUser, ColorType.RED)
            }
            progressGreen.onProgressChanged { progress, fromUser ->
                vm.onSeekBarChanged(progress, fromUser, ColorType.GREEN)
            }
            progressBlue.onProgressChanged { progress, fromUser ->
                vm.onSeekBarChanged(progress, fromUser, ColorType.BLUE)
            }
            progressAlpha.onProgressChanged { progress, fromUser ->
                vm.onSeekBarChanged(progress, fromUser, ColorType.ALPHA)
            }

            colorCircle.setOnTouchListener(View.OnTouchListener { v, event ->
                if (event.action != MotionEvent.ACTION_DOWN && event.action != MotionEvent.ACTION_MOVE) return@OnTouchListener true
                binding.colorCircle.drawToBitmap().let { bitmap ->
                    if (event.x >= 0 && event.y >= 0 && event.y < bitmap.height && event.x < bitmap.width) {
                        val pixelColor = bitmap.getPixel(event.x.toInt(), event.y.toInt())
                        val red = Color.red(pixelColor)
                        val green = Color.green(pixelColor)
                        val blue = Color.blue(pixelColor)
                        if (red > 0 && green > 0 && blue > 0) {
                            vm.onValidCircleTouchReceived(red, green, blue)
                            v.performClick()
                        }
                    }
                }
                true
            })
        }
    }

    private fun initObservers() {
        vm.colorValueFlow.collectWhenStarted(viewLifecycleOwner) { color ->
            binding.apply {
                newColorCard.setCardBackgroundColor(color)

                Color.red(color).let { red ->
                    progressRed.progress = red
                    tvRed.text = red.toString()
                }
                Color.green(color).let { green ->
                    progressGreen.progress = green
                    tvGreen.text = green.toString()
                }
                Color.blue(color).let { blue ->
                    progressBlue.progress = blue
                    tvBlue.text = blue.toString()
                }
                Color.alpha(color).let { alpha ->
                    progressAlpha.progress = alpha
                    tvAlpha.text = alpha.toString()
                }
            }
        }

        vm.hexColorValueFlow.collectWhenStarted(viewLifecycleOwner) { hex ->
            binding.etHexValue.setText(hex)
        }
    }
}