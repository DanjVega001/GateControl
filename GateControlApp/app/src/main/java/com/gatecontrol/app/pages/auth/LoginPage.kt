package com.gatecontrol.app.pages.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.gatecontrol.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.MultiFactorSession
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
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
                        Log.d("ee","ee")

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

        val userRef = db.collection("users").whereEqualTo("email", email)
        var isTwoFactorAuth:Boolean = false

        userRef.get().addOnSuccessListener {
            if (it != null) {
                for (doc in it){
                    isTwoFactorAuth = doc.data["twoStepAuthEnabled"] as Boolean
                }
            }
        }.addOnFailureListener {
            Log.d("ERROR",it.toString())
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
        }

        if (!isTwoFactorAuth){
            startActivity(Intent(this, HomePage::class.java).apply {
                putExtra("email", email)
            })
        } else {
            startActivity(Intent(this, HomePage::class.java).apply {
                putExtra("email", email)
            })
            /*user?.multiFactor?.session?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val multiFactorSession: MultiFactorSession? = task.result
                    val phoneAuthOptions = PhoneAuthOptions.newBuilder()
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(30L, TimeUnit.SECONDS)
                        .setMultiFactorSession(multiFactorSession!!)
                        .setCallbacks(callbacks)
                        .build()
                    // Send SMS verification code.
                    PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)

                }
            }*/
        }

    }



    /*private fun configurePhoneAuthCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // La verificación se completó automáticamente
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Error durante la verificación del número de teléfono
                Toast.makeText(this@PhoneVerificationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // El código de verificación fue enviado al número de teléfono
                this@PhoneVerificationActivity.verificationId = verificationId
                // Aquí puedes mostrar una pantalla para que el usuario ingrese el código de verificación
            }
        }
    }*/


    private fun showError(errorMessage:String){
        Log.d("Error", errorMessage)
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
}