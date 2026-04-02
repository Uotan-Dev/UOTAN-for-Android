package com.uotan.forum.resource.data.model

sealed class ResourceType {
    object Old : ResourceType()
    object New : ResourceType()
    object Other : ResourceType()
}