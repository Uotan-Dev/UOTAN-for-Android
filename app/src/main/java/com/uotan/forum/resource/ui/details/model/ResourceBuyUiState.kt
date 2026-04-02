package com.uotan.forum.resource.ui.details.model

import com.uotan.forum.resource.data.model.PurchaseData

sealed class ResourceBuyUiState {
    object Idle : ResourceBuyUiState()
    object Loading : ResourceBuyUiState()
    data class ShowNewResourceBuyDialog(val purchaseData: MutableList<PurchaseData>)
        : ResourceBuyUiState()
    data class ShowOldResourceBuyDialog(val purchaseData: MutableList<PurchaseData>)
        : ResourceBuyUiState()
    data class Error(val title: Int, val message: String?) : ResourceBuyUiState()
}