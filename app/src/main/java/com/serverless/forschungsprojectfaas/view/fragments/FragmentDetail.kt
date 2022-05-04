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
import com.serverless.forschungsprojectfaas.extensions.collectWhenStarted
import com.serverless.forschungsprojectfaas.extensions.disableChangeAnimation
import com.serverless.forschungsprojectfaas.extensions.hiltNavDestinationViewModels
import com.serverless.forschungsprojectfaas.extensions.onClick
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
        }
    }

    private fun initObservers() {
        vm.entryTitleFlow.collectWhenStarted(viewLifecycleOwner) { title ->
            binding.pageTitle.text = title
        }

        vm.imageBitmapFlow.collectWhenStarted(viewLifecycleOwner) { bitmap ->
            binding.progress.isVisible = false
            binding.iv.setImage(ImageSource.bitmap(bitmap))
        }

        vm.barBatchWithBarsStateFlow.collectWhenStarted(viewLifecycleOwner) {
            rva.submitList(it.sortedByDescending { it.bars.size })
            binding.iv.setRectangles(it)
        }

        vm.allBarsFlow.collectWhenStarted(viewLifecycleOwner) { bars ->
            binding.tvSticksAmount.text = if (bars.isEmpty()) "-" else bars.size.toString()
        }
    }

//    private fun generateRandomRects(bitmap: Bitmap) = launch {
//        val rects: MutableList<RectF> = mutableListOf()
//        val rectSize = bitmap.width / 100f
//        val maxRectsHeight = bitmap.height / rectSize
//        for (i in 0..Random.nextInt(1500)) {
//            val randomNumber = Random.nextInt(100)
//            val randomHeight = Random.nextInt(maxRectsHeight.toInt())
//
//            val left = randomNumber * rectSize
//            val right = left + rectSize
//            val bottom = randomHeight * rectSize
//            val top = bottom + rectSize
//            val rect = RectF(left, top, right, bottom)
//            rects.add(rect)
//        }
//        binding.iv.setImageDots(rects)
//    }
}