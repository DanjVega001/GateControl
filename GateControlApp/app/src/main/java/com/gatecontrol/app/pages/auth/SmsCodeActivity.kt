package com.gatecontrol.app.pages.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.gatecontrol.app.R
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class SmsCodeActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var callbacks: OnVerificationStateChangedCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms_code)
        getCallbacks()
        val bundle:Bundle? = intent.extras
        val userId:String = bundle?.getString("user_id").orEmpty()
        lifecycleScope.launch {
            loadNumberPhone(userId)
            sendCode()
        }
    }

    private fun sendCode(){
        val userFirebase:FirebaseUser? =  FirebaseAuth.getInstance().currentUser
        val txtPhoneNumberSendSmsCode = findViewById<TextView>(R.id.txtPhoneNumberSendSmsCode)

        userFirebase!!.multiFactor.session.addOnCompleteListener {
            if (it.isSuccessful) {
                val multiFactorSession: MultiFactorSession = it.result
                val phoneAuthOptions = PhoneAuthOptions.newBuilder()
                    .setPhoneNumber(txtPhoneNumberSendSmsCode.text.toString())
                    .setTimeout(30L, TimeUnit.SECONDS)
                    .setMultiFactorSession(multiFactorSession)
                    .setCallbacks(callbacks)
                    .build()
                // Send SMS verification code.
                PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
            }
        }


    }

    private suspend fun loadNumberPhone(userId:String){
        val user = db.collection("users").document(userId)
            .get().await()
        val txtPhoneNumberSendSmsCode = findViewById<TextView>(R.id.txtPhoneNumberSendSmsCode)
        if (user.exists()){
            val phoneNumber:String = user.data!!["phone"].toString()
            txtPhoneNumberSendSmsCode.text = "*** ****${phoneNumber.substring(7, 10)}"
        }
    }

    private fun getCallbacks(){
        callbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1) Instant verification. In some cases, the phone number can be
                //    instantly verified without needing to send or enter a verification
                //    code. You can disable this feature by calling
                //    PhoneAuthOptions.builder#requireSmsValidation(true) when building
                //    the options to pass to PhoneAuthProvider#verifyPhoneNumber().
                // 2) Auto-retrieval. On some devices, Google Play services can
                //    automatically detect the incoming verification SMS and perform
                //    verification without user action.
                //this@SmsCodeActivity.credential = credential
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in response to invalid requests for
                // verification, like an incorrect phone number.
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }
                // Show a message and update the UI
                // ...
            }

            override fun onCodeSent(
                verificationId: String, forceResendingToken: ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number.
                // We now need to ask the user to enter the code and then construct a
                // credential by combining the code with a verification ID.
                // Save the verification ID and resending token for later use.
                //this@MainActivity.verificationId = verificationId
                //this@MainActivity.forceResendingToken = forceResendingToken
                // ...
            }
        }
    }
}