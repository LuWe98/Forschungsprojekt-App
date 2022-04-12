package com.serverless.forschungsprojectfaas.view.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.serverless.forschungsprojectfaas.databinding.BsdfSelectionBinding
import com.serverless.forschungsprojectfaas.extensions.disableChangeAnimation
import com.serverless.forschungsprojectfaas.extensions.getThemeColor
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingBottomSheetDialogFragment
import com.serverless.forschungsprojectfaas.view.recyclerview.RvaSelectionDialog
import com.serverless.forschungsprojectfaas.viewmodel.VmSelectionDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BsdfSelection : BindingBottomSheetDialogFragment<BsdfSelectionBinding>() {

    private val vmSelection: VmSelectionDialog by viewModels()

    private lateinit var rvAdapter: RvaSelectionDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iniViews()
    }

    private fun iniViews() {
        binding.tvTitle.text = vmSelection.selectionType.titleProvider(requireContext())

        vmSelection.selectionType.titleProvider(requireContext()).let { title ->
            if(title == null) {
                binding.tvTitle.isVisible = false
            } else {
                binding.tvTitle.text = title
            }
        }

        rvAdapter = RvaSelectionDialog().apply {
            onItemClicked = vmSelection::onItemSelected
            selectionPredicate = vmSelection::isItemSelected
            selectionColor = getThemeColor(com.google.android.material.R.attr.colorOnBackground)
        }

        binding.rv.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(false)
            disableChangeAnimation()
        }

        rvAdapter.submitList(vmSelection.selectionType.recyclerViewList)
    }
}