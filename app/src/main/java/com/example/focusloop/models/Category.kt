package com.example.focusloop.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.focusloop.R


data class Category(
    val id: Int,  // Un identificador único para cada categoría
    val name: String // El nombre de la categoría
)
