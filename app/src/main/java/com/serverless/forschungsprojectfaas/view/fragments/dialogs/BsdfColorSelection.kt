package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.view.drawToBitmap
import androidx.fragment.app.viewModels
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.BsdfColorSelectionBinding
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingBottomSheetDialogFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmColorSelection

class BsdfColorSelection() : BindingBottomSheetDialogFragment<BsdfColorSelectionBinding>() {

    private val vm by hiltNavDestinationViewModels<VmColorSelection>(R.id.bsdfColorSelection)

//    private var final_hex: String? = null
//    private val color_array = IntArray(4)
//    private var backgroundColorDrawable: GradientDrawable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        enableFullscreenMode()
        enableNonCollapsing()
        bottomSheetBehaviour?.halfExpandedRatio = 0.9999999f

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {

    }

    private fun initListeners() {
        binding.apply {
            colorCircle.setOnTouchListener(onTouchListener)
            btnBack.onClick(vm::onCancelButtonClicked)

            seekBarGreen.onProgressChanged { progress, fromUser ->

            }
            seekBarRed.onProgressChanged { progress, fromUser ->

            }
            seekBarBlue.onProgressChanged { progress, fromUser ->

            }
            seekBarOpacity.onProgressChanged { progress, fromUser ->

            }

//            seekBarOpacity.setOnSeekBarChangeListener(seekbarListener("opacity"))
//            seekBarRed.setOnSeekBarChangeListener(seekbarListener("red"))
//            seekBarGreen.setOnSeekBarChangeListener(seekbarListener("green"))
//            seekBarBlue.setOnSeekBarChangeListener(seekbarListener("blue"))
        }
    }

    private fun initObservers() {
        vm.colorValueFlow.collectWhenStarted(viewLifecycleOwner) {
            log("COLOR CHANGED: $it")
        }
    }


    //    private fun setUpViews() {
////        backgroundColorDrawable = binding.relLayoutNewColor.background as GradientDrawable
////        val oldBackground = binding.relLayoutOldColor.background as GradientDrawable
////        oldBackground.setColor(currentColor)
//
//        binding.colorCircle.setOnTouchListener(onTouchListener)
//        binding.buttonSave.setOnClickListener {
//            val color = Color.parseColor(final_hex)
//        }
//    }
//
    private val onTouchListener = View.OnTouchListener { v, event ->
        if (event.action != MotionEvent.ACTION_DOWN && event.action != MotionEvent.ACTION_MOVE) return@OnTouchListener true
        binding.colorCircle.drawToBitmap().let { bitmap ->
            if (event.x >= 0 && event.y >= 0 && event.y < bitmap.height && event.x < bitmap.width) {
                val pixelColor = bitmap.getPixel(event.x.toInt(), event.y.toInt())
                val red = Color.red(pixelColor)
                val green = Color.green(pixelColor)
                val blue = Color.blue(pixelColor)
                if (red > 0 && green > 0 && blue > 0) {
                    vm.onValidCircleTouchReceived(red, green, blue)
//                    setSeekProgress()
//                    setBackgroundColorAndText(false)
                    v.performClick()
                }
            }
        }
        true
    }
//
//    private fun seekbarListener(color: String) = object : OnSeekBarChangeListener {
//        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//            val index: Int = when (color) {
//                "red" -> 0
//                "green" -> 1
//                "blue" -> 2
//                else -> 3
//            }
//            color_array[index] = (255.toFloat() * (progress.toFloat() / 100)).toInt()
//            setBackgroundColorAndText(false)
//        }
//
//        override fun onStartTrackingTouch(seekBar: SeekBar) {}
//        override fun onStopTrackingTouch(seekBar: SeekBar) {}
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun setBackgroundColorAndText(start: Boolean) {
//        var red_hex = Integer.toHexString(color_array[0])
//        var green_hex = Integer.toHexString(color_array[1])
//        var blue_hex = Integer.toHexString(color_array[2])
//        var opacity_hex = Integer.toHexString(color_array[3])
//        if (red_hex.length == 1) {
//            red_hex = "0$red_hex"
//        }
//        if (green_hex.length == 1) {
//            green_hex = "0$green_hex"
//        }
//        if (blue_hex.length == 1) {
//            blue_hex = "0$blue_hex"
//        }
//        if (opacity_hex.length == 1) {
//            opacity_hex = "0$opacity_hex"
//        }
//        final_hex = "#$opacity_hex$red_hex$green_hex$blue_hex"
//        if (!start) {
//            backgroundColorDrawable.setColor(Color.parseColor(final_hex))
//        } else {
//            color_array[0] = Color.red(tabColor)
//            color_array[1] = Color.green(tabColor)
//            color_array[2] = Color.blue(tabColor)
//            color_array[3] = Color.alpha(tabColor)
//            setSeekProgress()
//            backgroundColorDrawable!!.setColor(tabColor)
//        }
//        binding.textViewRed.text = "R: " + color_array[0]
//        binding.textViewGreen.text = "G: " + color_array[1]
//        binding.textViewBlue.text = "B: " + color_array[2]
//        binding.textViewOpacity.text = "O: " + color_array[3]
//        binding.textViewHex.text = final_hex
//    }
//
//    private fun setSeekProgress() {
//        binding.seekBarRed.progress = (color_array[0].toFloat() / 255 * 100).toInt()
//        binding.seekBarGreen.progress = (color_array[1].toFloat() / 255 * 100).toInt()
//        binding.seekBarBlue.progress = (color_array[2].toFloat() / 255 * 100).toInt()
//        binding.seekBarOpacity.progress = (color_array[3].toFloat() / 255 * 100).toInt()
//    }
}