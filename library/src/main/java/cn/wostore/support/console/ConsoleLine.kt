package cn.wostore.support.console

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

data class ConsoleLine(
    val pid: Int,
    val tid: Int,
    val time: Date?,
    val level: String,
    val tag: String,
    var msg: String
) {
    companion object {

        private val THREADTIME_LINE: Pattern =
            Pattern.compile("^(\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d{3})(?:\\s+[0-9A-Za-z]+)?\\s+(\\d+)\\s+(\\d+)\\s+([A-Z])\\s+(.+?)\\s*: (.*)$")

        private val year by lazy {
            val yearFormatter: DateFormat = SimpleDateFormat("yyyy", Locale.CHINA)
            yearFormatter.format(Date())
        }

        private fun parseTime(timeStr: String): Date? {
            val formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA)
            return try {
                formatter.parse("$year-$timeStr")
            } catch (e: ParseException) {
                null
            }
        }

        fun from(rawLine: String): ConsoleLine? {
            val m: Matcher = THREADTIME_LINE.matcher(rawLine)
            return if (m.matches()) {
                ConsoleLine(
                    m.group(2)!!.toInt(),
                    m.group(3)!!.toInt(),
                    parseTime(m.group(1)!!),
                    m.group(4)!!,
                    m.group(5)!!,
                    m.group(6)!!
                )
            } else {
                null
            }
        }
    }
}
