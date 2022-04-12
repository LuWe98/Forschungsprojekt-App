package com.serverless.forschungsprojectfaas.view.fragments

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.FragmentDeviceCamBinding
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import com.serverless.forschungsprojectfaas.viewmodel.VmAddOld
import com.serverless.forschungsprojectfaas.viewmodel.VmAddOld.FragmentMainEvent
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentAddOld : BindingFragment<FragmentDeviceCamBinding>() {

    private val vm: VmAddOld by hiltNavDestinationViewModels(R.id.fragmentAdd)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rotateImageView(vm.currentRotation)
        initListeners()
        initObservers()
    }

    private val askForPermission = askForPermission { vm.onPermissionResultReceived(it) }

    private val onCameraResult = registerForResult { vm.onCameraResultReceived(it) }

    private val onBrowseFolderResult = registerForResult { vm.onFilePickerResultReceived(it) }


    private fun initListeners() {
        binding.apply {
            photoButton.onClick {
                askForPermission.launch(Manifest.permission.CAMERA)
            }
            loadButton.onClick {
                val getIntent = Intent(Intent.ACTION_GET_CONTENT)
                getIntent.type = "image/*"

                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                pickIntent.type = "image/*"

                val chooserIntent = Intent.createChooser(getIntent, "Select Image")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

                onBrowseFolderResult.launch(chooserIntent)
            }

            imageView.onClick(vm::onPictureClicked)
        }
    }

    private fun rotateImageView(rotateBy: Int) {
        binding.imageView.rotation = rotateBy.toFloat()
        binding.imageView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            if (rotateBy == 90) {
                val displayMetrics = requireContext().resources.displayMetrics
                val bottomMargin = requireActivity().resources.getDimension(R.dimen.grid_24)
                val sideMargin = requireActivity().resources.getDimension(R.dimen.grid_2)

                width = displayMetrics.heightPixels - bottomMargin.toInt() - sideMargin.toInt()
                height = displayMetrics.widthPixels - sideMargin.toInt() * 2
            } else {
                height = 0
                width = 0
            }
        }
    }

    private fun initObservers() {
        vm.fragmentMainEventChannelFlow.collectWhenStarted(viewLifecycleOwner) { event ->
            when (event) {
                is FragmentMainEvent.LaunchActivityResult -> onCameraResult.launch(event.intent)
                is FragmentMainEvent.SetImageBitmapAsBackground -> setPreviewImage(event.bitmap, event.rotatedBy)
                is FragmentMainEvent.ShowMessageSnackBar -> showSnackBar(event.messageRes)
                is FragmentMainEvent.RotateImageView -> rotateImageView(event.rotatedBy)
            }
        }
    }

    private fun setPreviewImage(bitmap: Bitmap, rotateBy: Int) {
        Glide.with(this).load(bitmap.rotate(rotateBy)).into(binding.imageView)
    }
}