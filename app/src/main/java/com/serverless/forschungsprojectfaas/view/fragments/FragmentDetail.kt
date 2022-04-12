package com.serverless.forschungsprojectfaas.view.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.databinding.FragmentDetailBinding
import com.serverless.forschungsprojectfaas.extensions.*
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaDetails
import com.serverless.forschungsprojectfaas.viewmodel.VmDetail
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FragmentDetail: BindingFragment<FragmentDetailBinding>() {

    private val vm: VmDetail by hiltNavDestinationViewModels(R.id.fragmentDetail)

    private lateinit var rva: RvaDetails

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews(){
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

        vm.imageUriFlow.collectWhenStarted(viewLifecycleOwner) { uri ->
            binding.progress.isVisible = true

            launch(IO) {
                BitmapFactory.decodeFile(uri.path)?.let { bitmap ->
                    withContext(Main) {
                        Glide.with(this@FragmentDetail).load(bitmap).into(binding.iv).also {
                            binding.progress.isVisible = false
                        }
                    }
                }
            }
        }

        vm.stickPlaceholderStateFlow.collectWhenStarted(viewLifecycleOwner) { sticks ->
            binding.tvSticksAmount.text = if(sticks.isEmpty()) "-" else sticks.size.toString()
            rva.submitList(sticks)
        }
    }
}