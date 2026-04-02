package com.uotan.forum.ui.view.effect

class BgEffectDataManager {
    val dataPhoneLight: BgEffectData
    val dataPadLight: BgEffectData
    val dataPhoneDark: BgEffectData
    val dataPadDark: BgEffectData

    init {
        dataPhoneLight = createPhoneLightData()
        dataPadLight = createPadLightData()
        dataPhoneDark = createPhoneDarkData()
        dataPadDark = createPadDarkData()
    }

    private fun createPhoneLightData(): BgEffectData {
        return BgEffectData().apply {
            uTranslateY = 0.0f
            uPoints = floatArrayOf(0.8f, 0.2f, 1.0f, 0.8f, 0.9f, 1.0f, 0.2f, 0.9f, 1.0f, 0.2f, 0.2f, 1.0f)
            uAlphaMulti = 1.0f
            uNoiseScale = 1.5f
            uPointOffset = 0.2f
            uPointRadiusMulti = 1.0f
            uSaturateOffset = 0.2f
            uLightOffset = 0.1f
            uAlphaOffset = 0.5f
            uShadowColorMulti = 0.3f
            uShadowColorOffset = 0.3f
            uShadowNoiseScale = 5.0f
            uShadowOffset = 0.01f
            colorInterpPeriod = 5.0f
            gradientSpeedChange = 1.6f
            gradientSpeedRest = 1.05f

            gradientColors1 = floatArrayOf(
                1.0f, 0.9f, 0.94f, 1.0f,
                1.0f, 0.84f, 0.89f, 1.0f,
                0.97f, 0.73f, 0.82f, 1.0f,
                0.64f, 0.65f, 0.98f, 1.0f
            )
            gradientColors2 = floatArrayOf(
                0.58f, 0.74f, 1.0f, 1.0f,
                1.0f, 0.9f, 0.93f, 1.0f,
                0.74f, 0.76f, 1.0f, 1.0f,
                0.97f, 0.77f, 0.84f, 1.0f
            )
            gradientColors3 = floatArrayOf(
                0.98f, 0.86f, 0.9f, 1.0f,
                0.6f, 0.73f, 0.98f, 1.0f,
                0.92f, 0.93f, 1.0f, 1.0f,
                0.56f, 0.69f, 1.0f, 1.0f
            )
        }
    }

    private fun createPadLightData(): BgEffectData {
        return BgEffectData().apply {
            uTranslateY = 0.0f
            uPoints = floatArrayOf(
                0.8f, 0.2f, 1.0f, 0.8f,
                0.9f, 1.0f, 0.2f, 0.9f,
                1.0f, 0.2f, 0.2f, 1.0f
            )
            uAlphaMulti = 1.0f
            uNoiseScale = 1.5f
            uPointOffset = 0.2f
            uPointRadiusMulti = 1.0f
            uSaturateOffset = 0.2f
            uLightOffset = 0.1f
            uAlphaOffset = 0.5f
            uShadowColorMulti = 0.3f
            uShadowColorOffset = 0.3f
            uShadowNoiseScale = 5.0f
            uShadowOffset = 0.01f
            colorInterpPeriod = 7.0f
            gradientSpeedChange = 1.8f
            gradientSpeedRest = 1.0f

            gradientColors1 = floatArrayOf(
                0.99f, 0.77f, 0.86f, 1.0f,
                0.74f, 0.76f, 1.0f, 1.0f,
                0.72f, 0.74f, 1.0f, 1.0f,
                0.98f, 0.76f, 0.8f, 1.0f
            )
            gradientColors2 = floatArrayOf(
                0.66f, 0.75f, 1.0f, 1.0f,
                1.0f, 0.86f, 0.91f, 1.0f,
                0.74f, 0.76f, 1.0f, 1.0f,
                0.97f, 0.77f, 0.84f, 1.0f
            )
            gradientColors3 = floatArrayOf(
                0.97f, 0.79f, 0.85f, 1.0f,
                0.65f, 0.68f, 0.98f, 1.0f,
                0.66f, 0.77f, 1.0f, 1.0f,
                0.72f, 0.73f, 0.98f, 1.0f
            )
        }
    }

    private fun createPhoneDarkData(): BgEffectData {
        return BgEffectData().apply {
            uTranslateY = 0.0f
            uPoints = floatArrayOf(
                0.8f, 0.2f, 1.0f, 0.8f,
                0.9f, 1.0f, 0.2f, 0.9f,
                1.0f, 0.2f, 0.2f, 1.0f
            )
            uAlphaMulti = 1.0f
            uNoiseScale = 1.5f
            uPointOffset = 0.4f
            uPointRadiusMulti = 1.0f
            uSaturateOffset = 0.17f
            uLightOffset = 0.0f
            uAlphaOffset = 0.5f
            uShadowColorMulti = 0.3f
            uShadowColorOffset = 0.3f
            uShadowNoiseScale = 5.0f
            uShadowOffset = 0.01f
            colorInterpPeriod = 8.0f
            gradientSpeedChange = 1.0f
            gradientSpeedRest = 1.0f

            gradientColors1 = floatArrayOf(
                0.2f, 0.06f, 0.88f, 0.4f,
                0.3f, 0.14f, 0.55f, 0.5f,
                0.0f, 0.64f, 0.96f, 0.5f,
                0.11f, 0.16f, 0.83f, 0.4f
            )
            gradientColors2 = floatArrayOf(
                0.07f, 0.15f, 0.79f, 0.5f,
                0.62f, 0.21f, 0.67f, 0.5f,
                0.06f, 0.25f, 0.84f, 0.5f,
                0.0f, 0.2f, 0.78f, 0.5f
            )
            gradientColors3 = floatArrayOf(
                0.58f, 0.3f, 0.74f, 0.4f,
                0.27f, 0.18f, 0.6f, 0.5f,
                0.66f, 0.26f, 0.62f, 0.5f,
                0.12f, 0.16f, 0.7f, 0.6f
            )
        }
    }

    private fun createPadDarkData(): BgEffectData {
        return BgEffectData().apply {
            uTranslateY = 0.0f
            uPoints = floatArrayOf(
                0.8f, 0.2f, 1.0f, 0.8f,
                0.9f, 1.0f, 0.2f, 0.9f,
                1.0f, 0.2f, 0.2f, 1.0f
            )
            uAlphaMulti = 1.0f
            uNoiseScale = 1.5f
            uPointOffset = 0.2f
            uPointRadiusMulti = 1.0f
            uSaturateOffset = 0.0f
            uLightOffset = 0.0f
            uAlphaOffset = 0.5f
            uShadowColorMulti = 0.3f
            uShadowColorOffset = 0.3f
            uShadowNoiseScale = 5.0f
            uShadowOffset = 0.01f
            colorInterpPeriod = 7.0f
            gradientSpeedChange = 1.6f
            gradientSpeedRest = 1.2f

            gradientColors1 = floatArrayOf(
                0.66f, 0.26f, 0.62f, 0.4f,
                0.06f, 0.25f, 0.84f, 0.5f,
                0.0f, 0.64f, 0.96f, 0.5f,
                0.14f, 0.18f, 0.55f, 0.5f
            )
            gradientColors2 = floatArrayOf(
                0.07f, 0.15f, 0.79f, 0.5f,
                0.11f, 0.16f, 0.83f, 0.5f,
                0.06f, 0.25f, 0.84f, 0.5f,
                0.66f, 0.26f, 0.62f, 0.5f
            )
            gradientColors3 = floatArrayOf(
                0.58f, 0.3f, 0.74f, 0.5f,
                0.11f, 0.16f, 0.83f, 0.5f,
                0.66f, 0.26f, 0.62f, 0.5f,
                0.27f, 0.18f, 0.6f, 0.6f
            )
        }
    }

    fun getData(deviceType: DeviceType, themeMode: ThemeMode): BgEffectData {
        return when (deviceType) {
            DeviceType.PHONE -> if (themeMode == ThemeMode.LIGHT) dataPhoneLight else dataPhoneDark
            DeviceType.TABLET -> if (themeMode == ThemeMode.LIGHT) dataPadLight else dataPadDark
        }
    }

    data class BgEffectData(
        var uTranslateY: Float = 0f,
        var uPoints: FloatArray? = null,
        var uAlphaMulti: Float = 0f,
        var uNoiseScale: Float = 0f,
        var uPointOffset: Float = 0f,
        var uPointRadiusMulti: Float = 0f,
        var uSaturateOffset: Float = 0f,
        var uLightOffset: Float = 0f,
        var uAlphaOffset: Float = 0f,
        var uShadowColorMulti: Float = 0f,
        var uShadowColorOffset: Float = 0f,
        var uShadowNoiseScale: Float = 0f,
        var uShadowOffset: Float = 0f,
        var colorInterpPeriod: Float = 0f,
        var gradientSpeedChange: Float = 0f,
        var gradientSpeedRest: Float = 0f,
        var gradientColors1: FloatArray? = null,
        var gradientColors2: FloatArray? = null,
        var gradientColors3: FloatArray? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BgEffectData

            if (uTranslateY != other.uTranslateY) return false
            if (uAlphaMulti != other.uAlphaMulti) return false
            if (uNoiseScale != other.uNoiseScale) return false
            if (uPointOffset != other.uPointOffset) return false
            if (uPointRadiusMulti != other.uPointRadiusMulti) return false
            if (uSaturateOffset != other.uSaturateOffset) return false
            if (uLightOffset != other.uLightOffset) return false
            if (uAlphaOffset != other.uAlphaOffset) return false
            if (uShadowColorMulti != other.uShadowColorMulti) return false
            if (uShadowColorOffset != other.uShadowColorOffset) return false
            if (uShadowNoiseScale != other.uShadowNoiseScale) return false
            if (uShadowOffset != other.uShadowOffset) return false
            if (colorInterpPeriod != other.colorInterpPeriod) return false
            if (gradientSpeedChange != other.gradientSpeedChange) return false
            if (gradientSpeedRest != other.gradientSpeedRest) return false
            if (!uPoints.contentEquals(other.uPoints)) return false
            if (!gradientColors1.contentEquals(other.gradientColors1)) return false
            if (!gradientColors2.contentEquals(other.gradientColors2)) return false
            if (!gradientColors3.contentEquals(other.gradientColors3)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = uTranslateY.hashCode()
            result = 31 * result + uAlphaMulti.hashCode()
            result = 31 * result + uNoiseScale.hashCode()
            result = 31 * result + uPointOffset.hashCode()
            result = 31 * result + uPointRadiusMulti.hashCode()
            result = 31 * result + uSaturateOffset.hashCode()
            result = 31 * result + uLightOffset.hashCode()
            result = 31 * result + uAlphaOffset.hashCode()
            result = 31 * result + uShadowColorMulti.hashCode()
            result = 31 * result + uShadowColorOffset.hashCode()
            result = 31 * result + uShadowNoiseScale.hashCode()
            result = 31 * result + uShadowOffset.hashCode()
            result = 31 * result + colorInterpPeriod.hashCode()
            result = 31 * result + gradientSpeedChange.hashCode()
            result = 31 * result + gradientSpeedRest.hashCode()
            result = 31 * result + (uPoints?.contentHashCode() ?: 0)
            result = 31 * result + (gradientColors1?.contentHashCode() ?: 0)
            result = 31 * result + (gradientColors2?.contentHashCode() ?: 0)
            result = 31 * result + (gradientColors3?.contentHashCode() ?: 0)
            return result
        }
    }

    enum class DeviceType { PHONE, TABLET }
    enum class ThemeMode { LIGHT, DARK }
}