package cn.wostore.support.console

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

fun Date.toFormattedTime(pattern: String = "yyyy-MM-dd HH:mm:ss.SSS"): String {
    val format = SimpleDateFormat(pattern, Locale.CHINA)
    return format.format(this)
}

fun Any.logD(msg: String) {
    Log.d("console_debug", msg)
}