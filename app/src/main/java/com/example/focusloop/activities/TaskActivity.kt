// activities/TaskActivity.kt
package com.example.focusloop.activities

import android.app.DatePickerDialog
import android.app.Dialog
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import com.example.focusloop.R
import com.example.focusloop.data.PrefsRepo
import com.example.focusloop.models.Category
import com.example.focusloop.models.Task
import com.example.focusloop.models.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

class TaskActivity : BaseActivity() {

    private lateinit var repo: PrefsRepo

    private lateinit var taskCategory: Spinner
    private lateinit var categoryAdapter: ArrayAdapter<String>
    private val categoryNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        repo = PrefsRepo(this)

        // Configuración del botón de "Atrás"
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            finish()  // Cierra la actividad y regresa a la actividad anterior
        }

        val taskName: EditText = findViewById(R.id.taskName)
        val taskDescription: EditText = findViewById(R.id.taskDescription)
        taskCategory = findViewById(R.id.taskCategory)
        val taskStatus: Spinner = findViewById(R.id.taskStatus)
        val startDate: EditText = findViewById(R.id.startDate)
        val endDate: EditText = findViewById(R.id.endDate)

        // Categorías: carga de prefs; si está vacío, agrega defaults y guarda
        categoryNames.clear()
        categoryNames.addAll(repo.getCategoryNames())
        if (categoryNames.isEmpty()) {
            categoryNames.addAll(listOf("Work", "Personal"))
            repo.saveCategoryNames(categoryNames)
        }

        categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskCategory.adapter = categoryAdapter

        val statuses = TaskStatus.values().map { it.displayName }
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskStatus.adapter = statusAdapter

        findViewById<Button>(R.id.newCategoryButton).setOnClickListener {
            showNewCategoryDialog()
        }
        findViewById<Button>(R.id.newCategoryButton).setOnLongClickListener {
            val sel = taskCategory.selectedItem?.toString() ?: return@setOnLongClickListener true
            if (categoryNames.size <= 1) {
                Toast.makeText(this, "There must be at least one category.", Toast.LENGTH_SHORT).show()
            } else {
                repo.removeCategoryName(sel)
                categoryNames.remove(sel)
                categoryAdapter.notifyDataSetChanged()
            }
            true
        }

        findViewById<Button>(R.id.cancelButton).setOnClickListener { finish() }

        // Cargar datos si es modo edición
        val isEditMode = intent.getBooleanExtra("edit_mode", false)
        val editPosition = intent.getIntExtra("task_position", -1)
        if (isEditMode && editPosition != -1) {
            // Rellenar los campos con los datos de la tarea
            taskName.setText(intent.getStringExtra("task_name") ?: "")
            taskDescription.setText(intent.getStringExtra("task_description") ?: "")
            val category = intent.getStringExtra("task_category") ?: ""
            val categoryIndex = categoryNames.indexOfFirst { it.equals(category, true) }
            if (categoryIndex >= 0) taskCategory.setSelection(categoryIndex)
            val status = intent.getStringExtra("task_status") ?: "NOT_STARTED"
            val statusIndex = statuses.indexOfFirst { it.equals(TaskStatus.valueOf(status).displayName, true) }
            if (statusIndex >= 0) taskStatus.setSelection(statusIndex)
            startDate.setText(intent.getStringExtra("task_start_date") ?: "")
            endDate.setText(intent.getStringExtra("task_end_date") ?: "")
            findViewById<Button>(R.id.createTaskButton).text = getString(R.string.create_task_button).replace("Create", "Save")
        }

        findViewById<Button>(R.id.createTaskButton).setOnClickListener {
            val name = taskName.text.toString().trim()
            val description = taskDescription.text.toString().trim()
            val categoryName = taskCategory.selectedItem?.toString() ?: ""
            val status = TaskStatus.fromString(taskStatus.selectedItem.toString())
            val startDateValue = startDate.text.toString().trim().ifEmpty { null }
            val endDateValue = endDate.text.toString().trim().ifEmpty { null }

            if (name.isEmpty() || description.isEmpty() || categoryName.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Validar que la fecha de fin no sea menor a la de inicio
            if (!startDateValue.isNullOrBlank() && !endDateValue.isNullOrBlank()) {
                val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val start = sdf.parse(startDateValue)
                val end = sdf.parse(endDateValue)
                if (start != null && end != null && end.before(start)) {
                    Toast.makeText(this, "End date cannot be before start date.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val task = Task(
                name = name,
                description = description,
                category = Category(0, categoryName),
                status = status,
                startDate = startDateValue, // ahora sí se guarda la fecha
                endDate = endDateValue
            )

            if (isEditMode && editPosition != -1) {
                // Actualizar tarea existente
                val tasks = repo.getTasks()
                if (editPosition in tasks.indices) {
                    tasks[editPosition] = task
                    repo.saveTasks(tasks)
                    Toast.makeText(this, "Task updated successfully.", Toast.LENGTH_SHORT).show()
                }
            } else {
                repo.addTask(task)
                Toast.makeText(this, "Task created successfully.", Toast.LENGTH_SHORT).show()
            }
            finish()
        }

        startDate.setOnClickListener {
            showDatePickerDialog(startDate)
        }
        endDate.setOnClickListener {
            val startDateStr = startDate.text.toString().trim()
            showDatePickerDialog(endDate, startDateStr)
        }
    }

    private fun showNewCategoryDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_new_category)
        dialog.setCancelable(true)

        val input = dialog.findViewById<EditText>(R.id.inputCategoryName)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialog.findViewById<Button>(R.id.btnAdd)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnAdd.setOnClickListener {
            val name = input.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter a category name.", Toast.LENGTH_SHORT).show()
            } else if (categoryNames.any { it.equals(name, ignoreCase = true) }) {
                Toast.makeText(this, "Category already exists.", Toast.LENGTH_SHORT).show()
            } else {
                repo.addCategoryName(name)
                categoryNames.add(name)
                categoryAdapter.notifyDataSetChanged()
                taskCategory.setSelection(categoryNames.lastIndex)
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun showDatePickerDialog(targetEditText: EditText, minDateStr: String? = null) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val selectedDateStr = sdf.format(selectedCalendar.time)
            if (targetEditText.id == R.id.endDate && !minDateStr.isNullOrBlank()) {
                val minDate = sdf.parse(minDateStr)
                if (minDate != null && selectedCalendar.time.before(minDate)) {
                    Toast.makeText(this, "End date cannot be before start date.", Toast.LENGTH_SHORT).show()
                    return@OnDateSetListener
                }
            }
            targetEditText.setText(selectedDateStr)
        }
        val dialog = DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }
}
