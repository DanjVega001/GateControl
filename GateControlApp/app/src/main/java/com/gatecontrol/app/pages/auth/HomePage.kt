package com.gatecontrol.app.pages.auth

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gatecontrol.app.R
import com.google.firebase.auth.FirebaseUser

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle:Bundle? = intent.extras
        val email:String = bundle?.getString("email").orEmpty()
        val isEmailVerified:Boolean? = bundle?.getBoolean("isEmailVerified")
        if (isEmailVerified != false) {
            Log.d("TAGT", isEmailVerified.toString())

            setContentView(R.layout.activity_home_page)
        } else {
            Log.d("TAGF", isEmailVerified.toString())

            setContentView(R.layout.fragment_inauthenticated)
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
                .edit().putString("email", email).apply()
        }

    }
}