package com.serverless.forschungsprojectfaas.extensions

import android.app.Activity
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun Fragment.isPermissionGranted(permission: String) = requireContext().isPermissionGranted(permission)

fun Fragment.askForPermission(action: (Boolean) -> (Unit)) = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    action.invoke(granted)
}

fun Fragment.askForPermissions(action: (Map<String, Boolean>) -> (Unit)) = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
    action.invoke(granted)
}

fun Fragment.registerForResult(action: (ActivityResult) -> Unit) = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        action(result)
    }
}

fun Fragment.getThemeColor(@AttrRes themeAttrId: Int) = requireContext().getThemeColor(themeAttrId)

//inline fun Fragment.launch(
//    dispatcher: CoroutineContext = EmptyCoroutineContext,
//    scope: CoroutineScope = lifecycleScope,
//    startDelay: Long = 0,
//    crossinline block: suspend CoroutineScope.() -> Unit
//) {
//    scope.launch(dispatcher) {
//        delay(startDelay)
//        block.invoke(this)
//    }
//}

@MainThread
inline fun <reified VM : ViewModel> Fragment.hiltNavDestinationViewModels(
    @IdRes destinationId: Int
) = lazy {
    getHiltNavDestinationViewModel<VM>(destinationId)
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.getHiltNavDestinationViewModel(
    @IdRes destinationId: Int
) = findNavController().getBackStackEntry(destinationId).let {
    ViewModelProvider(it, HiltViewModelFactory(requireContext(), it))[VM::class.java]
}

@MainThread
fun Fragment.showSnackBar(
    text: String,
    viewToAttachTo: View = requireView(),
    anchorView: View? = null,
    animationMode: Int = Snackbar.ANIMATION_MODE_SLIDE,
    duration: Int = Snackbar.LENGTH_LONG
) = Snackbar.make(viewToAttachTo, text, duration).apply {
    setAnchorView(anchorView)
    this.animationMode = animationMode
    show()
}

@MainThread
fun Fragment.showSnackBar(
    @StringRes textRes: Int,
    viewToAttachTo: View = requireView(),
    anchorView: View? = null,
    animationMode: Int = Snackbar.ANIMATION_MODE_SLIDE,
    duration: Int = Snackbar.LENGTH_LONG
) = showSnackBar(getString(textRes), viewToAttachTo, anchorView, animationMode, duration)