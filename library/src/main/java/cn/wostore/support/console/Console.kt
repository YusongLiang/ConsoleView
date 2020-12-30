package cn.wostore.support.console

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

internal class Console(
    private val scope: CoroutineScope
) {

    private var rawLines: StringBuffer = StringBuffer()
    private var _stateUpdateListener: StateUpdateListener? = null
    private var _job: Job? = null

    fun start(stateUpdateListener: StateUpdateListener) {
        _stateUpdateListener = stateUpdateListener
        _job = scope.launch(Dispatchers.IO) {
            run()
        }
    }

    fun stop() {
        _job?.cancel()
        _job = null
    }

    fun clear() {
        rawLines.delete(0, rawLines.lastIndex)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun run() {
        withContext(Dispatchers.IO) {
            val builder = ProcessBuilder().command("logcat", "-b", "all", "-v", "threadtime", "*:V")
            builder.environment()["LC_ALL"] = "C"
            var process: Process? = null
            try {
                process = try {
                    builder.start()
                } catch (e: IOException) {
                    return@withContext
                }
                val stdout =
                    BufferedReader(InputStreamReader(process!!.inputStream, Charsets.UTF_8))
                _stateUpdateListener?.onStart()
                while (true) {
                    val rawLine = stdout.readLine() ?: break
                    rawLines.append(rawLine)
                    rawLines.append('\n')
                    val line = ConsoleLine.from(rawLine)
                    withContext(Dispatchers.Main.immediate) {
                        _stateUpdateListener?.onNewLine(line, rawLine, stdout.ready())
                    }
                }
            } finally {
                process?.destroy()
                _stateUpdateListener?.onStop()
            }
        }
    }

    interface StateUpdateListener {
        fun onStart()
        fun onNewLine(line: ConsoleLine?, rawLine: String, readyToRead: Boolean)
        fun onStop()
    }
}