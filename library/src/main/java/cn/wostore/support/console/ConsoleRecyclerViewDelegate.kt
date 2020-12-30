package cn.wostore.support.console

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

internal class ConsoleRecyclerViewDelegate(
    delegator: ConsoleView,
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) :
    ConsoleView.AbstractConsoleDelegate(delegator, context) {
    private val recyclerView: RecyclerView = RecyclerView(context)
    private lateinit var consoleLineAdapter: ConsoleLineAdapter
    private var haveScrolled = false
    private var lastScrollTime = 0L
    private var startTime = 0L

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.ConsoleView, defStyleAttr, defStyleRes
        )
        @Suppress("DEPRECATION") val lineTheme = ConsoleTheme(
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogError,
                context.resources.getColor(R.color.defaultErrorColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogErrorLight,
                context.resources.getColor(R.color.defaultErrorLightColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogWarning,
                context.resources.getColor(R.color.defaultWarningColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogWarningLight,
                context.resources.getColor(R.color.defaultWarningLightColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogInfo,
                context.resources.getColor(R.color.defaultInfoColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogInfoLight,
                context.resources.getColor(R.color.defaultInfoLightColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogDebug,
                context.resources.getColor(R.color.defaultDebugColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogDebugLight,
                context.resources.getColor(R.color.defaultDebugLightColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogVerbose,
                context.resources.getColor(R.color.defaultVerboseColor)
            ),
            typedArray.getColor(
                R.styleable.ConsoleView_colorLogVerboseLight,
                context.resources.getColor(R.color.defaultVerboseLightColor)
            )
        )
        typedArray.recycle()
        val lp = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        delegator.addView(recyclerView, lp)
        setupRecyclerView(lineTheme)
        delegator.invalidate()
    }

    private fun setupRecyclerView(theme: ConsoleTheme) {
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.isVerticalScrollBarEnabled = true
        consoleLineAdapter = ConsoleLineAdapter(theme)
        recyclerView.adapter = consoleLineAdapter
    }

    override fun onConsoleStart() {
        haveScrolled = false
        startTime = System.nanoTime()
        lastScrollTime = startTime
    }

    override fun onNewLine(line: ConsoleLine?, rawLine: String, readyToRead: Boolean) {
        if (line != null) {
            recyclerView.let {
                val shouldScroll = haveScrolled && !it.canScrollVertically(1)
                consoleLineAdapter.addLine(line, rawLine)
                if (haveScrolled) consoleLineAdapter.notifyDataSetChanged()
                if (shouldScroll) it.scrollToPosition(consoleLineAdapter.itemCount - 1)
            }
        } else {
            consoleLineAdapter.addLine(line, rawLine)
            if (haveScrolled) consoleLineAdapter.notifyDataSetChanged()
        }
        if (!haveScrolled) {
            val currentTime = System.nanoTime()
            val doScroll = (currentTime - startTime) > 2_500_000_000L || !readyToRead
            if (consoleLineAdapter.totalLineCount != 0 &&
                (doScroll || currentTime - lastScrollTime > 250_000_000L)
            ) {
                consoleLineAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(consoleLineAdapter.itemCount - 1)
                lastScrollTime = currentTime
            }
            if (doScroll) haveScrolled = true
        }
    }

    override fun onConsoleStop() {
        consoleLineAdapter.setLines(null)
    }

    override fun onFilterUpdated(level: Level, text: String) {
        val constraint = "${level.text}//${text.trim()}"
        consoleLineAdapter.filter.filter(constraint)
    }
}