package com.example.focusloop.activities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.focusloop.R
import com.example.focusloop.data.PrefsRepo
import com.example.focusloop.models.Task
import com.example.focusloop.models.TaskListAdapter
import java.text.SimpleDateFormat
import java.util.*

class MyTasksActivity : BaseActivity() {

    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskList: MutableList<Task>
    private lateinit var filteredTaskList: MutableList<Task>
    private lateinit var emptyStateLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_task)

        // Solicitar permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        // Botón de regreso
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }

        // Configurar RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val prefsRepo = PrefsRepo(this)
        taskList = prefsRepo.getTasks() // Recuperamos las tareas desde PrefsRepo
        filteredTaskList = taskList.toMutableList() // Inicializamos la lista filtrada con todas las tareas

        // Configuramos el Adapter para el RecyclerView
        taskListAdapter = TaskListAdapter(filteredTaskList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskListAdapter

        // Obtener los Spinner para estado y categoría
        val statusSpinner: Spinner = findViewById(R.id.statusSpinner)
        val categorySpinner: Spinner = findViewById(R.id.categorySpinner)

        // Cargar categorías dinámicamente y agregar "All"
        val userCategories = prefsRepo.getCategoryNames().toMutableList()
        if (!userCategories.contains("All")) {
            userCategories.add(0, "All")
        }
        val categoryAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, userCategories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Configurar los listeners para los Spinner
        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterTasks(statusSpinner.selectedItem.toString(), categorySpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: View?, position: Int, id: Long) {
                filterTasks(statusSpinner.selectedItem.toString(), categorySpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {}
        }

        // Configuración del campo de búsqueda
        val searchTask: EditText = findViewById(R.id.searchTask)
        searchTask.addTextChangedListener {
            val query = it.toString()
            filterTasks(statusSpinner.selectedItem.toString(), categorySpinner.selectedItem.toString(), query)
        }

        emptyStateLayout = findViewById(R.id.emptyStateLayout)
    }

    // Filtrar las tareas según el estado y la categoría seleccionados
    private fun filterTasks(status: String, category: String, query: String = "") {
        filteredTaskList.clear()

        val normalizedStatus = status.trim().lowercase().replace("_", " ")
        val normalizedCategory = category.trim().lowercase()

        filteredTaskList.addAll(
            taskList.filter {
                (
                    normalizedStatus == "all" ||
                    it.status.name.lowercase().replace("_", " ") == normalizedStatus ||
                    it.status.displayName.lowercase() == normalizedStatus
                ) &&
                (
                    normalizedCategory == "all" ||
                    it.category.name.trim().lowercase() == normalizedCategory
                ) &&
                (
                    it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
                )
            }
        )

        // Mostrar u ocultar el mensaje de "no hay tareas"
        if (filteredTaskList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
        } else {
            emptyStateLayout.visibility = View.GONE
        }

        taskListAdapter.notifyDataSetChanged() // Actualizamos el RecyclerView con las tareas filtradas
    }

    override fun onResume() {
        super.onResume()
        // Recargar la lista de tareas desde PrefsRepo y actualizar el adapter
        val prefsRepo = PrefsRepo(this)
        taskList.clear()
        taskList.addAll(prefsRepo.getTasks())
        filterTasks(
            findViewById<Spinner>(R.id.statusSpinner).selectedItem.toString(),
            findViewById<Spinner>(R.id.categorySpinner).selectedItem.toString(),
            findViewById<EditText>(R.id.searchTask).text.toString()
        )
    }
}
