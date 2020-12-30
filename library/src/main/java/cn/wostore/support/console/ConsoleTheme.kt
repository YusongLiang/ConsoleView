package cn.wostore.support.console

import androidx.annotation.ColorInt

data class ConsoleTheme(
    @ColorInt
    val colorError: Int,
    @ColorInt
    val colorErrorLight: Int,
    @ColorInt
    val colorWarning: Int,
    @ColorInt
    val colorWarningLight: Int,
    @ColorInt
    val colorInfo: Int,
    @ColorInt
    val colorInfoLight: Int,
    @ColorInt
    val colorDebug: Int,
    @ColorInt
    val colorDebugLight: Int,
    @ColorInt
    val colorVerbose: Int,
    @ColorInt
    val colorVerboseLight: Int
) {

    fun colorOf(level: String): Int {
        return when (level) {
            Level.ERROR.text -> colorError
            Level.WARNING.text -> colorWarning
            Level.INFO.text -> colorInfo
            Level.DEBUG.text -> colorDebug
            Level.VERBOSE.text -> colorVerbose
            else -> colorVerbose
        }
    }

    fun lightColorOf(level: String): Int {
        return when (level) {
            Level.ERROR.text -> colorErrorLight
            Level.WARNING.text -> colorWarningLight
            Level.INFO.text -> colorInfoLight
            Level.DEBUG.text -> colorDebugLight
            Level.VERBOSE.text -> colorVerboseLight
            else -> colorVerboseLight
        }
    }
}