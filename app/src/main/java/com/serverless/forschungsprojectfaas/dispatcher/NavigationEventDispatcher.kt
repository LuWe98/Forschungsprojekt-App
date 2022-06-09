package com.serverless.forschungsprojectfaas.dispatcher

import androidx.annotation.StringRes
import com.serverless.forschungsprojectfaas.NavGraphDirections
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.base.DispatchEvent
import com.serverless.forschungsprojectfaas.dispatcher.base.Dispatcher
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionRequestType
import com.serverless.forschungsprojectfaas.extensions.currentDestinationId
import com.serverless.forschungsprojectfaas.extensions.log
import com.serverless.forschungsprojectfaas.extensions.navController
import com.serverless.forschungsprojectfaas.model.PileEvaluation
import com.serverless.forschungsprojectfaas.model.room.entities.Batch
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.model.room.junctions.PileWithBatches
import com.serverless.forschungsprojectfaas.view.ActivityMain
import com.serverless.forschungsprojectfaas.view.fragments.FragmentDetail
import com.serverless.forschungsprojectfaas.view.fragments.FragmentDetailDirections
import com.serverless.forschungsprojectfaas.view.fragments.FragmentHomeDirections
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.BsdfBatchSelectionDirections
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.DfAddEditBatchDirections
import com.serverless.forschungsprojectfaas.view.fragments.dialogs.DfAlert
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

        class NavigateToDetailScreen(pile: Pile): NavigationEvent({
            navController.navigate(FragmentHomeDirections.actionFragmentHomeToFragmentDetail(pile))
        })

        class NavigateToLoadingDialog(@StringRes messageRes: Int): NavigationEvent({
            navController.navigate(NavGraphDirections.actionGlobalDfLoading(messageRes))
        })

        class NavigateToBatchSelection(): NavigationEvent({
            if(navController.currentDestinationId != R.id.bsdfBatchSelection) {
                navController.navigate(NavGraphDirections.actionGlobalBsdfBatchSelection())
            }
        })

        class NavigateToAddEditBatch(batch: Batch? = null): NavigationEvent({
            if(navController.currentDestinationId != R.id.dfAddEditBatch) {
                navController.navigate(NavGraphDirections.actionGlobalDfAddEditBatch(batch))
            }
        })

        class FromAddEditBatchToColorSelection(currentColor: Int): NavigationEvent({
            if(navController.currentDestinationId != R.id.bsdfColorSelection) {
                navController.navigate(DfAddEditBatchDirections.actionDfAddEditBatchToBsdfColorSelection(currentColor))
            }
        })

        object FromDetailsToRowMappingDialog: NavigationEvent({
            if(navController.currentDestinationId != R.id.bsdfRowMappingResults) {
                navController.navigate(FragmentDetailDirections.actionFragmentDetailToBsdfRowMappingResults())
            }
        })

        object PopLoadingDialog : NavigationEvent({
            if (navController.currentDestinationId  == R.id.dfLoading) {
                navController.popBackStack()
            }
        })

        class NavigateToSelectionDialog(selectionRequestType: SelectionRequestType<*>): NavigationEvent({
            navController.navigate(NavGraphDirections.actionGlobalBsdfSelection(selectionRequestType))
        })

        class NavigateToExportPileEvaluationResult(pileEvaluation: PileEvaluation? = null) : NavigationEvent({
            if(navController.currentDestinationId != R.id.dfExportBarEvaluationResults) {
                navController.navigate(NavGraphDirections.actionGlobalDfExportBarEvaluationResults(pileEvaluation))
            }
        })

        class NavigateToAlertDialog(message: DfAlert.AlertMessage): NavigationEvent({
            if(navController.currentDestinationId != R.id.dfAlert) {
                navController.navigate(NavGraphDirections.actionGlobalDfAlert(message))
            }
        })
    }
}