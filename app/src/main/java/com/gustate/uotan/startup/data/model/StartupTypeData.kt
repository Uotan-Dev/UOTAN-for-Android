package com.gustate.uotan.startup.data.model

data class StartupTypeData(
    val isSmsVerify: Boolean = false,
    val isAgreement: Boolean = false,
    val agreementXfToken: String = ""
)
