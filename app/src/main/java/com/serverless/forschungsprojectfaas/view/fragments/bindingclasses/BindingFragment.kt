package com.serverless.forschungsprojectfaas.view.fragments.bindingclasses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.serverless.forschungsprojectfaas.view.fragments.bindingclasses.BindingHelper.getBindingInline
import kotlin.reflect.KClass

abstract class BindingFragment<VB : ViewBinding>(private val clazz: KClass<VB>) : Fragment() {

    private var _binding: VB? = null
    val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        getBindingInline(clazz).also { _binding = it }.root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}