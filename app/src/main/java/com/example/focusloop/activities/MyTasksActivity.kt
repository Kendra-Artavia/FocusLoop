package com.example.focusloop.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.focusloop.R
import com.example.focusloop.data.PrefsRepo
import com.example.focusloop.models.Task
import com.example.focusloop.models.TaskListAdapter

class MyTasksActivity : BaseActivity() {

    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var taskList: MutableList<Task>
    private lateinit var filteredTaskList: MutableList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_task)

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
    }

    // Filtrar las tareas según el estado y la categoría seleccionados
    private fun filterTasks(status: String, category: String, query: String = "") {
        filteredTaskList.clear()

        filteredTaskList.addAll(
            taskList.filter {
                (it.status.displayName == status || status == "All") &&
                        (it.category.name == category || category == "All") &&
                        (it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true))
            }
        )

        taskListAdapter.notifyDataSetChanged() // Actualizamos el RecyclerView con las tareas filtradas
    }
}
