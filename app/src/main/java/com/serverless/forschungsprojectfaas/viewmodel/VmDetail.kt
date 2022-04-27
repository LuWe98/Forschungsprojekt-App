package com.serverless.forschungsprojectfaas.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher
import com.serverless.forschungsprojectfaas.dispatcher.NavigationEventDispatcher.NavigationEvent
import com.serverless.forschungsprojectfaas.extensions.launch
import com.serverless.forschungsprojectfaas.model.room.LocalRepository
import com.serverless.forschungsprojectfaas.model.room.entities.PictureEntry
import com.serverless.forschungsprojectfaas.model.room.entities.StickEntry
import com.serverless.forschungsprojectfaas.view.fragments.FragmentDetailArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class VmDetail @Inject constructor(
    private val navDispatcher: NavigationEventDispatcher,
    private val localRepository: LocalRepository,
    private val state: SavedStateHandle,
) : ViewModel() {

    private val args = FragmentDetailArgs.fromSavedStateHandle(state)

    private val pictureEntryStateFlow = localRepository
        .getPictureEntryFlowWithId(args.pictureEntry.id)
        .stateIn(viewModelScope, SharingStarted.Lazily, args.pictureEntry)

    val imageUriFlow = pictureEntryStateFlow.map(PictureEntry::pictureUri::get).distinctUntilChanged()

    val entryTitleFlow = pictureEntryStateFlow.map(PictureEntry::title::get).distinctUntilChanged()

    fun onBackButtonClicked() = launch(IO) {
        navDispatcher.dispatch(NavigationEvent.NavigateBack)
    }


    val stickPlaceholderStateFlow = MutableStateFlow(Array(10) {
        StickEntry(designation = "A", pictureEntryId = pictureEntryStateFlow.value.id)
    }.toList())


}