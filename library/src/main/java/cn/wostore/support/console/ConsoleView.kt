package cn.wostore.support.console

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class ConsoleView : FrameLayout {

    companion object {
        private const val VIEW_MODE_RECYCLER_VIEW = 1
    }

    private val delegate: ConsoleDelegate
    private val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + Job()
    private val console: Console

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
            super(context, attrs, defStyleAttr, defStyleRes) {
        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.ConsoleView, defStyleAttr, defStyleRes
        )
        val mode = typedArray.getInt(R.styleable.ConsoleView_viewMode, VIEW_MODE_RECYCLER_VIEW)
        delegate = when (mode) {
            VIEW_MODE_RECYCLER_VIEW -> ConsoleRecyclerViewDelegate(
                this, context, attrs, defStyleAttr, defStyleRes
            )
            else -> throw IllegalArgumentException("invalid viewMode attribute: $mode")
        }
        typedArray.recycle()
        console = Console(CoroutineScope(coroutineContext))
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    fun start() = console.run {
        start(object : Console.StateUpdateListener {
            override fun onStart() = delegate.onConsoleStart()

            override fun onNewLine(line: ConsoleLine?, rawLine: String, readyToRead: Boolean) =
                delegate.onNewLine(line, rawLine, readyToRead)

            override fun onStop() = delegate.onConsoleStop()
        })
    }

    fun filter(level: Level = Level.VERBOSE, text: String = "") {
        delegate.onFilterUpdated(level, text)
    }

    fun stop() = console.stop()

    internal interface ConsoleDelegate {
        fun onConsoleStart()
        fun onNewLine(line: ConsoleLine?, rawLine: String, readyToRead: Boolean)
        fun onConsoleStop()
        fun onFilterUpdated(level: Level, text: String)
    }

    internal abstract class AbstractConsoleDelegate(
        protected val delegator: ConsoleView,
        protected val context: Context
    ) : ConsoleDelegate
}