package com.gatecontrol.app.pages.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.gatecontrol.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle:Bundle? = intent.extras
        val email:String = bundle?.getString("email").orEmpty()
        val userFirebase:FirebaseUser =  FirebaseAuth.getInstance().currentUser!!

        Log.d("e", userFirebase.isEmailVerified.toString())
        userFirebase.reload().addOnSuccessListener {
            if (userFirebase.isEmailVerified) {
                getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
                    .edit().putString("email", email).apply()
                setContentView(R.layout.activity_home_page)
            } else {
                val inflatedView = layoutInflater.inflate(R.layout.fragment_inauthenticated, null)
                setContentView(inflatedView)
                val btnReload:Button = inflatedView.findViewById(R.id.btnReload)
                btnReload.setOnClickListener {
                    finish()
                    startActivity(intent)
                }
            }

        }


    }



}