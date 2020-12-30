package cn.wostore.support.console.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import cn.wostore.support.console.Level

class LevelSpinnerAdapter : BaseAdapter(), SpinnerAdapter {

    private val levels = Level.values()

    override fun getCount(): Int = levels.size

    override fun getItem(position: Int): Level = levels[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val tag = convertView?.tag
        val holder = if (tag is ViewHolder) tag else ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_spinner, parent, false)
        ).apply { itemView.tag = this }
        holder.tvTitle.text = getItem(position).text
        return holder.itemView
    }

    private class ViewHolder(val itemView: View) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
    }
}