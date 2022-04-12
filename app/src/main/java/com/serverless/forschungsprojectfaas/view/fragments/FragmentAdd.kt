package com.serverless.forschungsprojectfaas.view.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.FragmentAddBinding
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmAdd
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

    private fun initViews(){
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
            btnSave.onClick(vm::onSaveButtonClicked)
            etPictureTitle.onTextChanged(vm::onTitleChanged)
        }
    }

    private fun initObservers() {
        vm.rotationStateFlow.collectWhenStarted(viewLifecycleOwner) { rotation ->
            rotateImageView(rotation)
        }

        vm.bitmapStateFlow.collectWhenStarted(viewLifecycleOwner) { bitmap ->
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
        binding.imageContainer.apply {
            rotation = rotateBy.toFloat()
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                if (rotateBy == 90 || rotateBy == 270) {
                    height = resources.displayMetrics.widthPixels
                    width = binding.imageWrapper.height
                } else {
                    height = 0
                    width = 0
                }
            }
        }
    }

    companion object {
        private const val IMAGE_IME_TYPE = "image/*"

        val chooserIntent get() = run {
            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
            getIntent.type = IMAGE_IME_TYPE

            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickIntent.type = IMAGE_IME_TYPE

            val chooserIntent = Intent.createChooser(getIntent, "Select Image")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        }
    }
}