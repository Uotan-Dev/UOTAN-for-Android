package com.uotan.forum.resource.ui.details.model

sealed class ResourceBuyNavEvent {
    data class OpenUrlInBrowser(val url: String) : ResourceBuyNavEvent()
}