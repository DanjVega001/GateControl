package com.gatecontrol.app.pages.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.gatecontrol.app.R
import com.gatecontrol.app.fragments.ActivatePhoneFragmentDirections
import com.gatecontrol.app.network.AuthApiCliente
import com.gatecontrol.app.network.AuthApiService
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.MultiFactorSession
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SmsCodeActivity : AppCompatActivity() {

    private val service: AuthApiService = AuthApiCliente.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var phoneNumber:String = ""
    private var email:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_code)
        val bundle:Bundle? = intent.extras
        val userId:String = bundle?.getString("user_id").orEmpty()
        lifecycleScope.launch {
            val async = async { loadNumberPhone(userId) }
            async.await()
            sendCode()
        }
    }

    private fun sendCode(){
        var codeFromRequest = 0

        try {
            lifecycleScope.launch(Dispatchers.IO){
                val deferred =
                    async { service.sendNumberPhone(this@SmsCodeActivity.phoneNumber, true) }

                val response = deferred.await()


                withContext(Dispatchers.Main){
                    if (response.isSuccessful) {
                        val data = response.body()
                        Toast.makeText(this@SmsCodeActivity, data?.get("message").toString(), Toast.LENGTH_LONG).show()
                        codeFromRequest = (data?.get("code") as Double).toInt()
                    } else {
                        Log.d("Error", response.toString())
                        Toast.makeText(this@SmsCodeActivity, "Error "+response.message(), Toast.LENGTH_LONG).show()
                    }
                }

                verifyCode(codeFromRequest)
            }
        } catch (e:Exception){
            Log.d("Error", e.toString())
            Toast.makeText(this@SmsCodeActivity, "Error "+e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyCode(code:Int){
        val txtCode:EditText = findViewById(R.id.txtCodeAuth)
        val btnSendCode:Button = findViewById(R.id.btnSendCodeAuth)

        btnSendCode.setOnClickListener {
            if (txtCode.text.trim().isEmpty()){
                Toast.makeText(this, "Field code cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (txtCode.text.toString().toInt() != code){
                Toast.makeText(this, "Incorrect code", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            startActivity(Intent(this@SmsCodeActivity, HomePage::class.java).apply {
                putExtra("email", email)
            })
        }


    }

    private suspend fun loadNumberPhone(userId:String){
        val user = db.collection("users").document(userId)
            .get().await()
        val txtPhoneNumberSendSmsCode = findViewById<TextView>(R.id.txtPhoneNumberSendSmsCode)
        if (user.exists()){
            val phoneNumber:String = user.data!!["phone"].toString()
            txtPhoneNumberSendSmsCode.text = "*** ****${phoneNumber.substring(7, 10)}"
            this.phoneNumber = phoneNumber
            email = user.data!!["email"].toString()
        }
    }



}