package com.gustate.uotan.resource.data.model

sealed class ResourceType {
    object Old : ResourceType()
    object New : ResourceType()
    object Other : ResourceType()
}