package com.gatecontrol.app.pages.auth

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gatecontrol.app.R

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        val bundle:Bundle? = intent.extras
        val email:String = bundle?.getString("email").orEmpty()
        getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            .edit().putString("email", email).apply()
    }
}