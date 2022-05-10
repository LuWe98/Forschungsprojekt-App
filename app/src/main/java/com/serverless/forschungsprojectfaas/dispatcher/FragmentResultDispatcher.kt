package com.serverless.forschungsprojectfaas.dispatcher

import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.Companion.getResultKey
import com.serverless.forschungsprojectfaas.dispatcher.base.DispatchEvent
import com.serverless.forschungsprojectfaas.dispatcher.base.Dispatcher
import com.serverless.forschungsprojectfaas.dispatcher.selection.PictureMoreOptions
import com.serverless.forschungsprojectfaas.dispatcher.selection.SelectionTypeItemMarker
import com.serverless.forschungsprojectfaas.dispatcher.selection.OrderByItem
import com.serverless.forschungsprojectfaas.extensions.currentNavHostFragment
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import com.serverless.forschungsprojectfaas.view.ActivityMain
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import kotlin.reflect.KClass

inline fun <reified ResultType : FragmentResultDispatcher.FragmentResult> Fragment.setFragmentResultEventListener(crossinline action: (ResultType) -> Unit) {
    setFragmentResultListener(getResultKey(ResultType::class)) { key, bundle ->
        bundle.getParcelable<ResultType>(key)?.let(action)
    }
}

@ActivityRetainedScoped
class FragmentResultDispatcher @Inject constructor(
    private val publisher: DispatchEventPublisher
) : Dispatcher<FragmentResultDispatcher.FragmentResult> {

    companion object {
        private const val FRAGMENT_RESULT_KEY_SUFFIX = "FragmentResultKey"

        fun <ResultType : FragmentResult> getResultKey(clazz: KClass<ResultType>) = clazz.simpleName.plus(FRAGMENT_RESULT_KEY_SUFFIX)
    }

    override suspend fun dispatch(event: FragmentResult) = publisher.dispatchToQueue(event)


    sealed class FragmentResult : Parcelable, DispatchEvent {

        private val resultKey: String get() = getResultKey(this::class)

        override suspend fun execute(activity: ActivityMain) {
            activity.currentNavHostFragment.setFragmentResult(resultKey, Bundle().apply {
                putParcelable(resultKey, this@FragmentResult)
            })
        }

        @Parcelize
        data class ColorPickerResult(val selectedColor: Int): FragmentResult()

        @Parcelize
        data class BatchSelectionResult(val batchId: String): FragmentResult()
    }

    sealed class SelectionResult<SelectedItemType : Enum<SelectedItemType>> : FragmentResult() {

        abstract val selectedItem: SelectionTypeItemMarker<SelectedItemType>

        @Parcelize
        data class PictureMoreOptionsSelectionResult(
            val calledOnPile: Pile,
            override val selectedItem: PictureMoreOptions
        ) : SelectionResult<PictureMoreOptions>()

        @Parcelize
        data class OrderBySelectionResult(
            override val selectedItem: OrderByItem
        ) : SelectionResult<OrderByItem>()

    }
}