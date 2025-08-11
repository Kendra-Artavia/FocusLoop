// activities/TaskActivity.kt
package com.example.focusloop.activities

import android.app.DatePickerDialog
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

        findViewById<Button>(R.id.createTaskButton).setOnClickListener {
            val name = taskName.text.toString().trim()
            val description = taskDescription.text.toString().trim()
            val categoryName = taskCategory.selectedItem?.toString() ?: ""
            val status = TaskStatus.fromString(taskStatus.selectedItem.toString())

            if (name.isEmpty() || description.isEmpty() || categoryName.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val task = Task(
                name = name,
                description = description,
                category = Category(0, categoryName),
                status = status,
                startDate = null, // si luego guardas fechas, pásalas aquí
                endDate = null
            )

            repo.addTask(task)
            Toast.makeText(this, "Task created successfully.", Toast.LENGTH_SHORT).show()
            finish()
        }

        startDate.setOnClickListener {
            showDatePickerDialog(startDate)
        }
        endDate.setOnClickListener {
            showDatePickerDialog(endDate)
        }
    }

    private fun showNewCategoryDialog() {
        val input = EditText(this).apply { hint = "Category name" }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("New Category")
            .setView(input)
            .setPositiveButton("Add") { d, _ ->
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
                }
                d.dismiss()
            }
            .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
            .show()
    }

    private fun showDatePickerDialog(targetEditText: EditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            targetEditText.setText(sdf.format(selectedCalendar.time))
        }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
