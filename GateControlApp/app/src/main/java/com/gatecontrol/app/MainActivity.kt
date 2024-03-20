package com.gatecontrol.app

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.gatecontrol.app.pages.auth.HomePage
import com.gatecontrol.app.pages.auth.LoginPage
import com.gatecontrol.app.pages.auth.SignupPage
import com.google.android.gms.auth.api.identity.SignInClient

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupBotones()
        session()
    }

    override fun onStart() {
        super.onStart()
        val layout:LinearLayout = findViewById(R.id.main_layout)
        layout.visibility = View.VISIBLE
    }

    private fun session(){
        val prfcs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prfcs.getString("email", null)
        val layout:LinearLayout = findViewById(R.id.main_layout)
        if (email!=null){
            layout.visibility = View.INVISIBLE
            showHome(email)
        }
    }

    private fun showHome(email:String){
        startActivity(Intent(this, HomePage::class.java).apply {
            putExtra("email", email)
        })
    }

    private fun setupBotones(){

        val btnGoLoginPage:Button = findViewById(R.id.btnGoLogin)
        val btnGoSignupPage:Button = findViewById(R.id.btnGoSignup)
        val btnGoogle:Button = findViewById(R.id.btnGoogle)

        btnGoLoginPage.setOnClickListener {
            startActivity(Intent(this, LoginPage::class.java))
        }

        btnGoSignupPage.setOnClickListener {
            startActivity(Intent(this, SignupPage::class.java))
        }

        btnGoogle.setOnClickListener {

            /*val signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_cliente_id))
                        .setFilterByAuthorizedAccounts(true)
                        .build())
                .setAutoSelectEnabled(true)
                .build()

            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_cliente_id))
                .requestEmail()
                .build()

            val googleClient =GoogleSignIn.getClient(this, googleConf)
            */
        }
    }
}