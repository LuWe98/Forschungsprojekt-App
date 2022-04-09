package com.serverless.forschungsprojectfaas.view.fragments

import android.os.Bundle
import android.view.View
import com.serverless.forschungsprojectfaas.databinding.FragmentMainBinding
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentMain : BindingFragment<FragmentMainBinding>(FragmentMainBinding::class) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}