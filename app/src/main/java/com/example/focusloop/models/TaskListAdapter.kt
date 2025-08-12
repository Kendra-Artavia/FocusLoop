package com.example.focusloop.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.focusloop.R

class TaskListAdapter(private val tasks: List<Task>) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskNameTextView: TextView = itemView.findViewById(R.id.taskName)
        private val taskDescriptionTextView: TextView = itemView.findViewById(R.id.taskDescription)
        private val taskStatusSpinner: Spinner = itemView.findViewById(R.id.taskStatusSpinner)

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            taskDescriptionTextView.text = task.description

            // Configura el spinner con los estados posibles
            val statuses = TaskStatus.values().map { it.toString() }
            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, statuses)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            taskStatusSpinner.adapter = adapter

            // Selecciona el estado actual
            val currentStatusIndex = TaskStatus.values().indexOf(task.status)
            taskStatusSpinner.setSelection(currentStatusIndex, false)

            // Controla la selección inicial para evitar disparar el listener al asignar el estado
            var firstSelection = true
            taskStatusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (firstSelection) {
                        firstSelection = false
                        return
                    }
                    val newStatus = TaskStatus.values()[position]
                    if (task.status != newStatus) {
                        task.status = newStatus
                        Toast.makeText(
                            itemView.context,
                            "Task '${task.name}' status changed to $newStatus",
                            Toast.LENGTH_SHORT
                        ).show()
                        notifyItemChanged(adapterPosition)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }
}