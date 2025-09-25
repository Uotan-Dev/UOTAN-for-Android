package com.gustate.uotan.resource.ui.details.model

import com.gustate.uotan.resource.data.model.PurchaseData

sealed class ResourceBuyUiState {
    object Idle : ResourceBuyUiState()
    object Loading : ResourceBuyUiState()
    data class ShowNewResourceBuyDialog(val purchaseData: MutableList<PurchaseData>)
        : ResourceBuyUiState()
    data class ShowOldResourceBuyDialog(val purchaseData: MutableList<PurchaseData>)
        : ResourceBuyUiState()
    data class Error(val title: Int, val message: String?) : ResourceBuyUiState()
}