package cn.wostore.support.console

enum class Level(val text: String, description: String, val value: Int) {
    ERROR("E", "Error", 5),
    WARNING("W", "Warning", 4),
    INFO("I", "Info", 3),
    DEBUG("D", "Debug", 2),
    VERBOSE("V", "Verbose", 1);

    companion object {
        fun parse(text: String): Level? {
            values().forEach {
                if (it.text == text) return it
            }
            return null
        }
    }

    fun notLowerThan(level: Level): Boolean {
        return value >= level.value
    }
}