package com.example.focusloop.data

import android.content.Context
import com.example.focusloop.models.Category
import com.example.focusloop.models.Task
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefsRepo(context: Context) {
    private val prefs = context.getSharedPreferences("focusloop_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val KEY_TASKS = "tasks_json"
    private val KEY_CATEGORIES = "categories_json"

    fun getTasks(): MutableList<Task> {
        val json = prefs.getString(KEY_TASKS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Task>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveTasks(tasks: List<Task>) {
        prefs.edit().putString(KEY_TASKS, gson.toJson(tasks)).apply()
    }

    fun addTask(task: Task) {
        val list = getTasks()
        list.add(task)
        saveTasks(list)
    }

    fun getCategoryNames(): MutableList<String> {
        val json = prefs.getString(KEY_CATEGORIES, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveCategoryNames(categories: List<String>) {
        prefs.edit().putString(KEY_CATEGORIES, gson.toJson(categories)).apply()
    }

    fun addCategoryName(name: String) {
        val list = getCategoryNames()
        if (list.none { it.equals(name, ignoreCase = true) }) {
            list.add(name)
            saveCategoryNames(list)
        }
    }

    fun removeCategoryName(name: String) {
        val list = getCategoryNames()
        list.removeAll { it.equals(name, ignoreCase = true) }
        saveCategoryNames(list)
    }
}
