package com.serverless.forschungsprojectfaas.view.fragments.bindingclasses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
object BindingHelper {

    private const val INFLATE_METHOD = "inflate"

    private fun findGenericTypeWith(classInstance: Any, genericClassToFind: Class<*>, relativePosition : Int): Class<*> {
        return try {
            (classInstance.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments
                .mapNotNull {
                    try {
                        it as Class<*>
                    } catch (e: Exception) {
                        null
                    }
                }.filter {
                    genericClassToFind.isAssignableFrom(it)
                }[relativePosition]
        }  catch (e: Exception) {
            throw IllegalArgumentException("Could not find generic Class '$genericClassToFind' of Instance '$classInstance'!")
        }
    }

    private fun <VB : ViewBinding> getBindingWith (classInstance: Any, layoutInflater: LayoutInflater, relativePosition : Int) =
        findGenericTypeWith(classInstance, ViewBinding::class.java, relativePosition)
            .getMethod(INFLATE_METHOD, LayoutInflater::class.java)
            .invoke(null, layoutInflater) as VB

    fun <VB : ViewBinding> getViewHolderBindingWith(adapter: RecyclerView.Adapter<*>, parent: ViewGroup, relativePosition : Int = 0) =
        findGenericTypeWith(adapter, ViewBinding::class.java, relativePosition)
            .getMethod(INFLATE_METHOD, LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
            .invoke(null, LayoutInflater.from(parent.context), parent, false) as VB


    fun <VB: ViewBinding> getViewHolderBinding(clazz: KClass<VB>, parent: ViewGroup) = clazz.java
        .getMethod(INFLATE_METHOD, LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        .invoke(null, LayoutInflater.from(parent.context), parent, false) as VB


    fun <VB : ViewBinding> getBinding(fragment: BindingFragment<VB>, relativePosition : Int = 0) =
        getBindingWith(fragment, fragment.layoutInflater, relativePosition) as VB

    fun <VB : ViewBinding> getBinding(fragment: BindingDialogFragment<VB>, relativePosition : Int = 0) =
        getBindingWith(fragment, fragment.layoutInflater, relativePosition) as VB

    fun <VB : ViewBinding> getBinding(dialog: BindingDialog<VB>, relativePosition : Int = 0) =
        getBindingWith(dialog, dialog.layoutInflater, relativePosition) as VB

    fun <VB : ViewBinding> getBinding(fragment: BindingBottomSheetDialogFragment<VB>, relativePosition : Int = 0) =
        getBindingWith(fragment, fragment.layoutInflater, relativePosition) as VB

}