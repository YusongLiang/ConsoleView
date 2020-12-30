package cn.wostore.support.console

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.lang.AssertionError
import java.lang.IllegalArgumentException

class ConsoleLineAdapter(
    private var theme: ConsoleTheme
) : RecyclerView.Adapter<ConsoleLineAdapter.VH>(), Filterable {

    private val consoleLines = arrayListOf<ConsoleLine>()
    val totalLineCount: Int
        get() = consoleLines.size
    private val filteredLines = arrayListOf<ConsoleLine>()

    private var lastConstraint: CharSequence? = ""
    private val wordFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? {
            if (constraint == lastConstraint) return null
            lastConstraint = constraint
            val filtered = if (constraint == null || constraint.trim() == "//") consoleLines
            else synchronized(consoleLines) {
                consoleLines.filter { line ->
                    line.shouldBeKept(constraint)
                }
            }
            return FilterResults().apply {
                values = filtered
                count = filtered.size
            }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null) {
                filteredLines.clear()
                @Suppress("UNCHECKED_CAST") val list = results.values as List<ConsoleLine>
                filteredLines.addAll(list)
            }
            notifyDataSetChanged()
            host?.scrollToPosition(itemCount - 1)
        }
    }

    private fun ConsoleLine.shouldBeKept(constraint: CharSequence?): Boolean {
        if (constraint.isNullOrBlank()) return true
        val separator = constraint.indexOf("//")
        if (separator == -1) return true
        val level = if (separator == 0) "" else constraint.substring(0, separator)
        val words =
            if (separator + 1 == constraint.lastIndex) ""
            else constraint.substring(separator + 2, constraint.length)
        if (level.isEmpty() && words.isNotEmpty())
            return this.tag.contains(words, true) || this.msg.contains(words, true)
        if (level.isEmpty()) {
            return if (words.isEmpty()) true
            else this.tag.contains(words, true) || this.msg.contains(words, true)
        } else {
            val target = Level.parse(level)
            val current = Level.parse(this.level)
            if (target == null || current == null) return true
            return if (words.isEmpty())
                current.notLowerThan(target)
            else
                current.notLowerThan(target) &&
                        (this.tag.contains(words, true) ||
                                this.msg.contains(words, true))
        }
    }

    private var host: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        host = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        host = null
    }

    fun addLine(line: ConsoleLine?, rawLine: String) {
        if (line != null) {
            synchronized(consoleLines) {
                consoleLines.add(line)
                if (line.shouldBeKept(lastConstraint)) {
                    filteredLines.add(line)
                    notifyDataSetChanged()
                }
            }
        } else {
            consoleLines.lastOrNull()?.apply {
                msg += "\n$rawLine"
                if (filteredLines.contains(this)) {
                    filteredLines.last().msg += "\n$rawLine"
                }
            }
        }
    }

    fun setLines(lines: List<ConsoleLine>?) {
        synchronized(consoleLines) {
            consoleLines.clear()
            if (!lines.isNullOrEmpty()) {
                consoleLines.addAll(lines)
                filter.filter(lastConstraint)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_console_line, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val line = filteredLines[position]
        var hideTime = false
        if (position > 0) {
            val prev = filteredLines[position - 1]
            hideTime = line.time == prev.time && line.tag == prev.tag
        }
        val color = theme.colorOf(line.level)
        val colorLight = theme.lightColorOf(line.level)
        holder.tvLevel.setBackgroundColor(color)
        holder.tvLevel.text = line.level
        holder.svContainer.setBackgroundColor(colorLight)
        holder.tvContent.text = line.msg
        if (hideTime) holder.tvHeader.visibility = View.GONE
        else {
            holder.tvHeader.visibility = View.VISIBLE
            val headerText = SpannableStringBuilder()
                .append(line.time?.toFormattedTime())
                .append(" ")
                .append(line.tag, StyleSpan(Typeface.BOLD), Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            holder.tvHeader.text = headerText
        }
    }

    override fun getItemCount(): Int = filteredLines.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLevel: TextView = itemView.findViewById(R.id.tv_level)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val tvHeader: TextView = itemView.findViewById(R.id.tv_header)
        val svContainer: HorizontalScrollView = itemView.findViewById(R.id.sv_container)
    }

    override fun getFilter(): Filter {
        return wordFilter
    }
}