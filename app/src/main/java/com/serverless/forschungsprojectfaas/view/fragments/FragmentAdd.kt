package com.serverless.forschungsprojectfaas.view.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.FragmentAddBinding
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmAdd
import com.welu.androidflowutils.collectWhenStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAdd : BindingFragment<FragmentAddBinding>() {

    private val vm: VmAdd by hiltNavDestinationViewModels(R.id.fragmentAdd)

    private val askForPermission = askForPermission { vm.onPermissionResultReceived(it) }

    private val onCameraResult = registerForResult { vm.onCameraResultReceived(it) }

    private val onBrowseFolderResult = registerForResult { vm.onFilePickerResultReceived(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        binding.apply {
            etPictureTitle.setText(vm.title)
        }
    }


    private fun initListeners() {
        binding.apply {
            btnCamera.onClick { askForPermission.launch(Manifest.permission.CAMERA) }
            btnFiles.onClick { onBrowseFolderResult.launch(chooserIntent) }
            btnRotate.onClick(vm::onPictureClicked)
            btnBack.onClick(vm::onBackButtonClicked)
            btnEvaluate.onClick(vm::onEvaluateButtonClicked)
            btnAdd.onClick(vm::onAddButtonClicked)
            etPictureTitle.onTextChanged(vm::onTitleChanged)
        }
    }

    private fun initObservers() {
        vm.rotationStateFlow.collectWhenStarted(viewLifecycleOwner) { rotation ->
            vm.bitmapStateFlow.value?.let {
                rotateImageView(rotation)
            }
        }

        vm.bitmapStateFlow.collectWhenStarted(viewLifecycleOwner) { bitmap ->
            binding.placeholderLayout.isVisible = bitmap == null
            Glide.with(this).load(bitmap).into(binding.imageContainer)
        }

        vm.fragmentMainEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is VmAdd.FragmentMainEvent.LaunchActivityResult -> onCameraResult.launch(event.intent)
                is VmAdd.FragmentMainEvent.ShowMessageSnackBar -> showSnackBar(event.messageRes, viewToAttachTo = binding.root)
            }
        }
    }

    private fun rotateImageView(rotateBy: Int) {
        binding.apply {
            imageWrapper.post {
                imageContainer.rotation = rotateBy.toFloat()
                imageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    if (rotateBy == 90 || rotateBy == 270) {
                        //val bitmapRatio = currentBitmap.height.toDouble() / currentBitmap.width.toDouble()
                        height = imageWrapper.width
                        width = imageWrapper.height
                    } else {
                        height = 0
                        width = 0
                    }
                }
            }
        }
    }

    companion object {
        private const val IMAGE_IME_TYPE = "image/*"
        private const val CHOOSER_TITLE = "Select Image"

        val chooserIntent: Intent
            get() = run {
                Intent().let { intent ->
                    intent.type = IMAGE_IME_TYPE
                    intent.action = Intent.ACTION_GET_CONTENT;
                    Intent.createChooser(intent, CHOOSER_TITLE)
                }
            }
    }
}