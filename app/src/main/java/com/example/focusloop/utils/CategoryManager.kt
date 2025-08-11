package com.example.focusloop.utils

import com.example.focusloop.models.Category

class CategoryManager {

    private val categories = mutableListOf<Category>()
    fun addCategory(name: String) { categories.add(Category(categories.size + 1, name)) }
    fun removeByName(name: String) { categories.removeAll { it.name.equals(name, true) } }
    fun getCategories(): List<Category> = categories
}