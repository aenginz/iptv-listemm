package com.aenginz.yapilacaklar

import android.app.Activity
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject

data class Task(val text: String, var done: Boolean = false)

class MainActivity : Activity() {

    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: ArrayAdapter<Task>
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.input)
        val addButton = findViewById<Button>(R.id.addButton)
        val list = findViewById<ListView>(R.id.list)
        emptyView = findViewById(R.id.empty)

        loadTasks()

        adapter = object : ArrayAdapter<Task>(
            this, android.R.layout.simple_list_item_1, tasks
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                val task = tasks[position]
                view.text = if (task.done) "✓  ${task.text}" else "○  ${task.text}"
                view.paintFlags = if (task.done) {
                    view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
                view.alpha = if (task.done) 0.5f else 1f
                return view
            }
        }
        list.adapter = adapter

        fun addTask() {
            val text = input.text.toString().trim()
            if (text.isEmpty()) return
            tasks.add(0, Task(text))
            input.text.clear()
            refresh()
        }

        addButton.setOnClickListener { addTask() }
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTask(); true
            } else false
        }

        // Dokun: tamamlandı olarak işaretle / işareti kaldır
        list.setOnItemClickListener { _, _, position, _ ->
            tasks[position].done = !tasks[position].done
            refresh()
        }

        // Basılı tut: sil
        list.setOnItemLongClickListener { _, _, position, _ ->
            val removed = tasks.removeAt(position)
            refresh()
            Toast.makeText(this, "Silindi: ${removed.text}", Toast.LENGTH_SHORT).show()
            true
        }

        refresh()
    }

    private fun refresh() {
        adapter.notifyDataSetChanged()
        emptyView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        saveTasks()
    }

    private fun saveTasks() {
        val array = JSONArray()
        tasks.forEach {
            array.put(JSONObject().put("text", it.text).put("done", it.done))
        }
        getPreferences(MODE_PRIVATE).edit().putString("tasks", array.toString()).apply()
    }

    private fun loadTasks() {
        val json = getPreferences(MODE_PRIVATE).getString("tasks", null) ?: return
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                tasks.add(Task(obj.getString("text"), obj.optBoolean("done")))
            }
        } catch (e: Exception) {
            tasks.clear()
        }
    }
}
