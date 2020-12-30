package cn.wostore.support.console.sample

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import cn.wostore.support.console.Level
import cn.wostore.support.console.logD
import cn.wostore.support.console.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var filterLevel: Level = Level.VERBOSE
    private var filterWords: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.consoleView.start()
        binding.etFilter.doOnTextChanged { text, _, _, _ ->
            filterWords = text.toString()
            updateFilter()
        }
        val adapter = LevelSpinnerAdapter()
        binding.spinnerLevel.adapter = adapter
        binding.spinnerLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                filterLevel = adapter.getItem(position)
                logD("pos = $position, level = ${filterLevel.text}")
                updateFilter()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        binding.spinnerLevel.setSelection(Level.VERBOSE.ordinal)
    }

    private fun updateFilter() {
        binding.consoleView.filter(filterLevel, filterWords)
    }

    override fun onDestroy() {
        binding.consoleView.stop()
        super.onDestroy()
    }
}