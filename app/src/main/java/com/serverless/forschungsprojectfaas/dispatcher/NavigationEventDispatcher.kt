package com.serverless.forschungsprojectfaas.dispatcher

import androidx.annotation.StringRes
import androidx.navigation.NavGraph
import com.serverless.forschungsprojectfaas.NavGraphDirections
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.base.DispatchEvent
import com.serverless.forschungsprojectfaas.dispatcher.base.Dispatcher
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionRequestType
import com.serverless.forschungsprojectfaas.extensions.navController
import com.serverless.forschungsprojectfaas.model.room.entities.PictureEntry
import com.serverless.forschungsprojectfaas.view.ActivityMain
import com.serverless.forschungsprojectfaas.view.fragments.FragmentHomeDirections
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class NavigationEventDispatcher @Inject constructor(
    private val publisher: DispatchEventPublisher
) : Dispatcher<NavigationEventDispatcher.NavigationEvent> {

    override suspend fun dispatch(event: NavigationEvent) = publisher.dispatchToQueue(event)

    sealed class NavigationEvent(private val navAction: ActivityMain.() -> Unit) : DispatchEvent {

        override suspend fun execute(activity: ActivityMain) {
            navAction(activity)
        }

        object NavigateBack: NavigationEvent({
            navController.popBackStack()
        })

        object NavigateToAddScreen: NavigationEvent({
            navController.navigate(FragmentHomeDirections.actionFragmentHomeToFragmentAdd())
        })

        class NavigateToDetailScreen(pictureEntry: PictureEntry): NavigationEvent({
            navController.navigate(FragmentHomeDirections.actionFragmentHomeToFragmentDetail(pictureEntry))
        })

        class NavigateToLoadingDialog(@StringRes messageRes: Int): NavigationEvent({
            navController.navigate(NavGraphDirections.actionGlobalDfLoading(messageRes))
        })

        object PopLoadingDialog : NavigationEvent({
            if (navController.backQueue[navController.backQueue.size - 1].destination.id == R.id.dfLoading) {
                navController.popBackStack()
            }
        })

        class NavigateToSelectionDialog(selectionRequestType: SelectionRequestType<*>): NavigationEvent({
            navController.navigate(NavGraphDirections.actionGlobalBsdfSelection(selectionRequestType))
        })
    }
}