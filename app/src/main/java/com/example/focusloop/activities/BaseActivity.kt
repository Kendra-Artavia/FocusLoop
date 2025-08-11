package com.example.focusloop.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BaseActivity", "onCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d("BaseActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("BaseActivity", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("BaseActivity", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("BaseActivity", "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BaseActivity", "onDestroy")
    }
}