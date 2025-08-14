package com.example.focusloop.models

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.focusloop.R
import com.example.focusloop.data.PrefsRepo

class TaskListAdapter(private val tasks: MutableList<Task>) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

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
        private val taskCategoryTextView: TextView = itemView.findViewById(R.id.taskCategory)
        private val taskIcon: ImageView = itemView.findViewById(R.id.taskIcon)
        private val taskIconLeft: ImageView = itemView.findViewById(R.id.taskIconLeft)
        private val taskStartDateTextView: TextView = itemView.findViewById(R.id.taskStartDate)
        private val taskEndDateTextView: TextView = itemView.findViewById(R.id.taskEndDate)

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            taskDescriptionTextView.text = task.description
            taskCategoryTextView.text = task.category.name

            // Mostrar fechas de inicio y fin si existen
            taskStartDateTextView.text = if (!task.startDate.isNullOrBlank()) "Start: ${task.startDate}" else ""
            taskEndDateTextView.text = if (!task.endDate.isNullOrBlank()) "End: ${task.endDate}" else ""

            // Mostrar los estados en minúsculas y con espacios
            val statuses = TaskStatus.entries.map {
                it.name.lowercase().replace("_", " ")
            }
            val adapter = ArrayAdapter(itemView.context, android.R.layout.simple_spinner_item, statuses)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            taskStatusSpinner.adapter = adapter

            // Selecciona el estado actual
            val currentStatusIndex = TaskStatus.entries.indexOf(task.status)
            taskStatusSpinner.setSelection(currentStatusIndex, false)

            // Cambia el fondo del spinner según el estado
            val bgRes = when (task.status) {
                TaskStatus.NOT_STARTED -> R.drawable.bg_status_todo // purple
                TaskStatus.IN_PROGRESS -> R.drawable.bg_status_inprogress // blue
                TaskStatus.COMPLETED -> R.drawable.bg_status_done // pink light
            }
            taskStatusSpinner.background = ContextCompat.getDrawable(itemView.context, bgRes)

            // Controla la selección inicial para evitar disparar el listener al asignar el estado
            var firstSelection = true
            taskStatusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (firstSelection) {
                        firstSelection = false
                        return
                    }
                    val newStatus = TaskStatus.entries[position]
                    if (task.status != newStatus) {
                        task.status = newStatus
                        // Cambia el fondo al nuevo estado
                        val newBgRes = when (newStatus) {
                            TaskStatus.NOT_STARTED -> R.drawable.bg_status_todo // purple
                            TaskStatus.IN_PROGRESS -> R.drawable.bg_status_inprogress // blue
                            TaskStatus.COMPLETED -> R.drawable.bg_status_done // pink light
                        }
                        taskStatusSpinner.background = ContextCompat.getDrawable(itemView.context, newBgRes)
                        Toast.makeText(
                            itemView.context,
                            "Task '${task.name}' status changed to ${newStatus.name.lowercase().replace("_", " ")}",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Guardar el cambio en PrefsRepo
                        val prefsRepo = PrefsRepo(itemView.context)
                        prefsRepo.saveTasks(tasks)
                        notifyItemChanged(adapterPosition)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            // Eliminar tarea al tocar el icono
            taskIcon.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Mostrar diálogo personalizado
                    val dialog = Dialog(itemView.context)
                    dialog.setContentView(R.layout.dialog_confirm_delete)
                    dialog.setCancelable(true)

                    val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
                    val btnAccept = dialog.findViewById<Button>(R.id.btnAccept)

                    btnCancel.setOnClickListener { dialog.dismiss() }
                    btnAccept.setOnClickListener {
                        tasks.removeAt(position)
                        val prefsRepo = PrefsRepo(itemView.context)
                        prefsRepo.saveTasks(tasks)
                        notifyItemRemoved(position)
                        Toast.makeText(itemView.context, "Task deleted", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    dialog.show()
                }
            }

            // Editar tarea al tocar el icono izquierdo
            taskIconLeft.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val context = itemView.context
                    val intent = android.content.Intent(context, com.example.focusloop.activities.TaskActivity::class.java)
                    intent.putExtra("edit_mode", true)
                    intent.putExtra("task_position", position)
                    intent.putExtra("task_name", task.name)
                    intent.putExtra("task_description", task.description)
                    intent.putExtra("task_category", task.category.name)
                    intent.putExtra("task_status", task.status.name)
                    intent.putExtra("task_start_date", task.startDate)
                    intent.putExtra("task_end_date", task.endDate)
                    context.startActivity(intent)
                }
            }
        }
    }
}