package com.example.ui.utils

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object SoundHapticHelper {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (_: Exception) {
            // Ignore if tone generator cannot be initialized
        }
    }

    private fun playTone(toneType: Int, duration: Int) {
        try {
            toneGenerator?.startTone(toneType, duration)
        } catch (_: Exception) {}
    }

    fun playCorrectWordSound(enabled: Boolean) {
        if (enabled) {
            playTone(ToneGenerator.TONE_PROP_ACK, 180)
        }
    }

    fun playBonusWordSound(enabled: Boolean) {
        if (enabled) {
            playTone(ToneGenerator.TONE_CDMA_PIP, 120)
        }
    }

    fun playErrorSound(enabled: Boolean) {
        if (enabled) {
            playTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 300)
        }
    }

    fun playConnectionSound(enabled: Boolean) {
        if (enabled) {
            playTone(ToneGenerator.TONE_PROP_BEEP, 40)
        }
    }

    fun playCompleteSound(enabled: Boolean) {
        if (enabled) {
            playTone(ToneGenerator.TONE_CDMA_HIGH_L, 400)
        }
    }

    fun playClickSound(enabled: Boolean) {
        if (enabled) {
            playTone(ToneGenerator.TONE_PROP_BEEP, 80)
        }
    }

    fun triggerLightHaptic(haptic: HapticFeedback, enabled: Boolean) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun triggerMediumHaptic(haptic: HapticFeedback, enabled: Boolean) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun triggerErrorHaptic(haptic: HapticFeedback, enabled: Boolean) {
        if (enabled) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
