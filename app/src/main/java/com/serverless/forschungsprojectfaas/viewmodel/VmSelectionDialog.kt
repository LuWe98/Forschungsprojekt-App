package com.serverless.forschungsprojectfaas.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionRequestType
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionTypeItemMarker
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.BsdfSelectionArgs
import com.welu.androidflowutils.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@HiltViewModel
class VmSelectionDialog @Inject constructor(
    state: SavedStateHandle,
    private val applicationScope: CoroutineScope,
    private val navDispatcher: NavigationEventDispatcher,
    private val fragmentResultDispatcher: FragmentResultDispatcher
) : ViewModel() {

    private val args = BsdfSelectionArgs.fromSavedStateHandle(state)

    val selectionType: SelectionRequestType<*> get() = args.selectionType

    fun onItemSelected(item: SelectionTypeItemMarker<*>) {
        launch(scope = applicationScope) {
            navDispatcher.dispatch(NavigationEvent.NavigateBack)

            selectionType.resultProvider(item).let { result ->
                fragmentResultDispatcher.dispatch(result)
            }
        }
    }

    fun isItemSelected(item: SelectionTypeItemMarker<*>) = selectionType.isItemSelectedProvider(item)

}