package com.gatecontrol.app.pages.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.gatecontrol.app.R
import com.gatecontrol.app.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignupPage : AppCompatActivity() {

    private val db:FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_page)

        setupBotones()
    }

    private fun setupBotones(){
        val btnSignup:Button = findViewById(R.id.btnSignup)

        btnSignup.setOnClickListener {
            signup()
        }
    }

    private fun signup() {
        val txtNombre: EditText = findViewById(R.id.txtNameSignup)
        val txtEmail: EditText = findViewById(R.id.txtEmailSignup)
        val txtPassword: EditText = findViewById(R.id.txtPasswordSignup)



        if (txtNombre.text.trim().isNotEmpty() && txtEmail.text.trim().isNotEmpty() && txtPassword.text.trim().isNotEmpty()){
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(txtEmail.text.toString(), txtPassword.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val user = User(txtNombre.text.trim().toString(), txtEmail.text.trim().toString())
                        sendVerificationEmail(user)
                        registerUser(user)
                    } else {
                        showError(it.exception?.message ?: "Error desconocido")
                    }

                }
        }
    }

    private fun sendVerificationEmail(user: User){
        val userFirebase:FirebaseUser? =  FirebaseAuth.getInstance().currentUser

        userFirebase!!.sendEmailVerification()
            .addOnSuccessListener {
                registerUser(user)
            }
            .addOnFailureListener {
                showError(it.message ?: "Error desconocido")
            }
    }

    private fun registerUser(user: User){
        val data = hashMapOf(
            "name" to user.getName(),
            "email" to user.getEmail(),
            "twoStepAuthEnabled" to user.isTwoStepAuthEnabled()
        )
        db.collection("users").add(data).addOnSuccessListener {
            showHome(user.getEmail())
        }.addOnFailureListener {
            showError(it.message.toString())
        }
    }

    private fun showHome(email:String){
        startActivity(Intent(this, HomePage::class.java).apply {
            putExtra("email", email)
        })
    }

    private fun showError(errorMessage:String){
        Log.d("Error", errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}