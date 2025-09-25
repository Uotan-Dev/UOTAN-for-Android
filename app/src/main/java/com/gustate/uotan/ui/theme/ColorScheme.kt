package com.gustate.uotan.ui.theme

import android.content.Context
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color

data class ColorScheme(
    val background: Color,
    val onBackgroundPrimary: Color,
    val onBackgroundSecondary: Color,
    val onBackgroundSubtitle: Color,
    val filledButton: Color,
    val onFilledButton: Color,
    val filledTonalButton: Color,
    val onFilledTonalButton: Color,
    val card: Color,
    val onCardPrimary: Color,
    val onCardSecondary: Color,
    val errorCard: Color,
    val onErrorCard: Color
)

fun uotanLightColorScheme(
    bkg: Color = background,
    onBkgPrimary: Color = onBackgroundPrimary,
    onBkgSecondary: Color = onBackgroundSecondary,
    onBkgSubtitle: Color = onBackgroundSubtitle,
    filledBtn: Color = filledButton,
    onFilledBtn: Color = onFilledButton,
    filledTonalBtn: Color = filledTonalButton,
    onFilledTonalBtn: Color = onFilledTonalButton,
    cd: Color = card,
    onCdPrimary: Color = onCardPrimary,
    onCdSecondary: Color = onCardSecondary,
    errorCd: Color = errorCard,
    onErrorCd: Color = onErrorCard
): ColorScheme =
    ColorScheme(
        background = bkg,
        onBackgroundPrimary = onBkgPrimary,
        onBackgroundSecondary = onBkgSecondary,
        onBackgroundSubtitle = onBkgSubtitle,
        filledButton = filledBtn,
        onFilledButton = onFilledBtn,
        filledTonalButton = filledTonalBtn,
        onFilledTonalButton = onFilledTonalBtn,
        card = cd,
        onCardPrimary = onCdPrimary,
        onCardSecondary = onCdSecondary,
        errorCard = errorCd,
        onErrorCard = onErrorCd
    )

fun uotanDarkColorScheme(
    bkg: Color = backgroundDark,
    onBkgPrimary: Color = onBackgroundPrimaryDark,
    onBkgSecondary: Color = onBackgroundSecondaryDark,
    onBkgSubtitle: Color = onBackgroundSubtitleDark,
    filledBtn: Color = filledButtonDark,
    onFilledBtn: Color = onFilledButtonDark,
    filledTonalBtn: Color = filledTonalButtonDark,
    onFilledTonalBtn: Color = onFilledTonalButtonDark,
    cd: Color = cardDark,
    onCdPrimary: Color = onCardPrimaryDark,
    onCdSecondary: Color = onCardSecondaryDark,
    errorCd: Color = errorCardDark,
    onErrorCd: Color = onErrorCardDark
): ColorScheme =
    ColorScheme(
        background = bkg,
        onBackgroundPrimary = onBkgPrimary,
        onBackgroundSecondary = onBkgSecondary,
        onBackgroundSubtitle = onBkgSubtitle,
        filledButton = filledBtn,
        onFilledButton = onFilledBtn,
        filledTonalButton = filledTonalBtn,
        onFilledTonalButton = onFilledTonalBtn,
        card = cd,
        onCardPrimary = onCdPrimary,
        onCardSecondary = onCdSecondary,
        errorCard = errorCd,
        onErrorCard = onErrorCd
    )

@RequiresApi(31)
internal fun dynamicLightColorScheme(context: Context): ColorScheme {
    val scheme = androidx.compose.material3.dynamicLightColorScheme(context)
    return uotanLightColorScheme(
        bkg = scheme.background,
        onBkgPrimary = scheme.onBackground,
        onBkgSecondary = scheme.onBackground.copy(alpha = 0.8f),
        onBkgSubtitle = scheme.onBackground.copy(alpha = 0.6f),
        filledBtn = scheme.primaryContainer,
        onFilledBtn = scheme.onPrimaryContainer,
        filledTonalBtn = scheme.secondaryContainer,
        onFilledTonalBtn = scheme.onSecondaryContainer,
        cd = scheme.surfaceContainer,
        onCdPrimary = scheme.onSurface,
        onCdSecondary = scheme.onSurface.copy(alpha = 0.8f),
        errorCd = scheme.errorContainer,
        onErrorCd = scheme.onErrorContainer
    )
}

@RequiresApi(31)
fun dynamicDarkColorScheme(context: Context): ColorScheme {
    val scheme = androidx.compose.material3.dynamicDarkColorScheme(context)
    return uotanDarkColorScheme(
        bkg = scheme.background,
        onBkgPrimary = scheme.onBackground,
        onBkgSecondary = scheme.onBackground.copy(alpha = 0.8f),
        onBkgSubtitle = scheme.onBackground.copy(alpha = 0.6f),
        filledBtn = scheme.primaryContainer,
        onFilledBtn = scheme.onPrimaryContainer,
        filledTonalBtn = scheme.secondaryContainer,
        onFilledTonalBtn = scheme.onSecondaryContainer,
        cd = scheme.surfaceContainer,
        onCdPrimary = scheme.onSurface,
        onCdSecondary = scheme.onSurface.copy(alpha = 0.8f),
        errorCd = scheme.errorContainer,
        onErrorCd = scheme.onErrorContainer
    )
}