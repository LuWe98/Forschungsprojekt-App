package com.serverless.forschungsprojectfaas.dispatcher.selection

import android.content.Context
import android.os.Parcelable
import com.serverless.forschungsprojectfaas.R
import com.serverless.forschungsprojectfaas.dispatcher.FragmentResultDispatcher.SelectionResult
import com.serverless.forschungsprojectfaas.model.room.entities.Pile
import kotlinx.parcelize.Parcelize

sealed class SelectionRequestType<T : Enum<T>>(
    val recyclerViewList: List<SelectionTypeItemMarker<T>>,
    val titleProvider: (Context) -> String?,
    val resultProvider: (SelectionTypeItemMarker<*>) -> (SelectionResult<T>),
    val isItemSelectedProvider: (SelectionTypeItemMarker<*>) -> Boolean = { false }
) : Parcelable {

    @Parcelize
    data class PictureMoreOptionsSelection(
        val pile: Pile,
        val itemsToDisplay: List<SelectionTypeItemMarker<PictureMoreOptions>> = PictureMoreOptions.notEvaluatedPileOptions
    ) : SelectionRequestType<PictureMoreOptions>(
        recyclerViewList = itemsToDisplay,
        titleProvider = { pile.title },
        resultProvider = { SelectionResult.PictureMoreOptionsSelectionResult(pile, it as PictureMoreOptions) }
    )

    @Parcelize
    data class OrderBySelection(
        val currentItem: OrderByItem
    ) : SelectionRequestType<OrderByItem>(
        recyclerViewList = OrderByItem.values().toList(),
        titleProvider = { it.getString(R.string.orderBy) },
        resultProvider = { SelectionResult.OrderBySelectionResult(it as OrderByItem) },
        isItemSelectedProvider = { it == currentItem }
    )

}