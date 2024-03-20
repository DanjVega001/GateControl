package com.gatecontrol.app.pages.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.gatecontrol.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.MultiFactorSession
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit


class LoginPage : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        setupBotones()
    }

    private fun setupBotones(){
        val btnBackSignup:Button = findViewById(R.id.btnBackSignup)
        val btnLogin:Button = findViewById(R.id.btnCHangePassword)
        val txtEmail:EditText = findViewById(R.id.txtNameEdit)
        val txtPassword:EditText = findViewById(R.id.txtPasswordLogin)


        btnBackSignup.setOnClickListener {
            finish()
            startActivity(Intent(this, SignupPage::class.java))
        }


        btnLogin.setOnClickListener {
            if (txtEmail.text.trim().isNotEmpty() && txtPassword.text.trim().isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(txtEmail.text.trim().toString(), txtPassword.text.trim().toString())
                    .addOnSuccessListener {
                        val email: String? = it.user?.email
                        val user = it.user
                        showHome(email.orEmpty(), user)
                    }
                    .addOnFailureListener {
                        showError(it.message.toString())
                    }
            }
        }
    }

    private fun showHome(email:String, user:FirebaseUser?) {

        var isTwoFactorAuth:Boolean = false
        var userId:String? = null

        lifecycleScope.launch {
            val userRef = db.collection("users").whereEqualTo("email", email)
                .get().await().documents

            if (userRef != null) {
                for (doc in userRef){
                    userId = doc.id
                    isTwoFactorAuth = doc.data!!["twoStepAuthEnabled"] as Boolean
                    Log.d("doc", doc.data!!["twoStepAuthEnabled"].toString())
                    Log.d("doc", doc.data!!["name"].toString())
                }
            } else {
                Log.d("doc", "Error")
                Toast.makeText(this@LoginPage, "Error al loguearse", Toast.LENGTH_LONG).show()
            }

            if (!isTwoFactorAuth){
                startActivity(Intent(this@LoginPage, HomePage::class.java).apply {
                    putExtra("email", email)
                })
            } else {
                startActivity(Intent(this@LoginPage, SmsCodeActivity::class.java).apply {
                    putExtra("user_id", userId)
                })
            }
        }
    }

    private fun showError(errorMessage:String){
        Log.d("Error", errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}