package com.gustate.uotan.resource.ui.details.model

sealed class ResourceBuyNavEvent {
    data class OpenUrlInBrowser(val url: String) : ResourceBuyNavEvent()
}