package com.gatecontrol.app.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gatecontrol.app.MainActivity
import com.gatecontrol.app.R
import com.gatecontrol.app.models.User
import com.gatecontrol.app.network.AuthApiCliente
import com.gatecontrol.app.pages.auth.LoginPage
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class SettingsFragment : Fragment() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val user: User = User("","")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        setupNav(root)
        lifecycleScope.launch(Dispatchers.Main){
            val async = async {
                loadProfileData(root)
                deleteAccount(root)
            }
            async.await()

            editName(root)
            logout(root)
            goModalChangePassword(root)
            configSwitches(root)
        }
        return root
    }

    private suspend fun loadProfileData(root: View) {
        val email: String? = context?.getSharedPreferences(
            getString(R.string.prefs_file), Context.MODE_PRIVATE
        )?.getString("email", "")

        email ?: return

        try {
            val userRef = db.collection("users").whereEqualTo("email", email).get().await()
            for (doc in userRef) {
                user.name  = doc.data["name"] as String
                user.email = doc.data["email"] as String
                user.twoStepAuthEnabled = doc.data["twoStepAuthEnabled"] as Boolean
                user.userId = doc.id
            }
            val txtName: EditText = root.findViewById(R.id.txtNameEdit)
            txtName.setText(user.name)
        } catch (e: Exception) {
            Log.d("ERROR", e.message ?: "")
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }



    private fun editName(root: View){

        val btnEdit:ImageButton = root.findViewById(R.id.btnEdit)

        btnEdit.setOnClickListener {
            val txtName:EditText = root.findViewById(R.id.txtNameEdit)

            if (txtName.text.trim().isEmpty()){
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_LONG).show()
            } else {
                db.collection("users").document(user.userId)
                    .update("name", txtName.text.toString())
                    .addOnSuccessListener {
                        findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
                        Toast.makeText(context, "User name updated correctly", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Log.d("ERROR", it.message.orEmpty())
                        Toast.makeText(context, "Error updating username", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun logout(root: View){
        val btnLogout:Button = root.findViewById(R.id.btnLogout)

        btnLogout.setOnClickListener {
            context?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)!!
                .edit().clear().apply()
            requireActivity().finish()
            startActivity(Intent(context, LoginPage::class.java))
        }
    }

    private fun goModalChangePassword(root: View){
        val btnGoChangePassword: Button = root.findViewById(R.id.btnModalChangePassword)

        btnGoChangePassword.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.fragment_modal_chenge_password)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            changePassword(dialog)
            dialog.show()
        }
    }

    private fun changePassword(dialog: Dialog){
        val currentUser = Firebase.auth.currentUser
        val textNewPassword:EditText = dialog.findViewById(R.id.txtNewPassword)
        val textRepeatPassword:EditText = dialog.findViewById(R.id.txtRepeatPassword)
        val btnChangePassword:Button = dialog.findViewById(R.id.btnChangePassword)

        btnChangePassword.setOnClickListener {
            if (textNewPassword.text.trim().isEmpty() || textRepeatPassword.text.trim().isEmpty()) {
                Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (textNewPassword.text.toString() != textRepeatPassword.text.toString()){
                Toast.makeText(context, "Passwords dont match", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            currentUser?.updatePassword(textNewPassword.text.toString())
                ?.addOnSuccessListener {
                    Toast.makeText(context, "Password updated correctly", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                ?.addOnFailureListener {
                    Log.d("Error", it.message.orEmpty())
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
        }
    }


    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private fun configSwitches(root: View){
        val swTwoFactorAuth:SwitchCompat = root.findViewById(R.id.swTwoFactorAuth)
        val argsFragment:SettingsFragmentArgs by navArgs()

        if (argsFragment.isCancel || !user.twoStepAuthEnabled){
            swTwoFactorAuth.isChecked = false
        } else {
            swTwoFactorAuth.isChecked = true
        }

        swTwoFactorAuth.setOnClickListener {

            if (swTwoFactorAuth.isChecked){
                findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToActivatePhoneFragment(
                    userId = user.userId
                ))
            } else {
                disableTwoStepAuth(root)
            }
        }
    }

    private fun disableTwoStepAuth(root: View){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.modal_confirm_delete_two_steps_auth)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val btnCancelarDeleteTwoStepsAuth:Button = dialog.findViewById(R.id.btnCancelarDeleteTwoStepsAuth)
        val btnImSure:Button = dialog.findViewById(R.id.btnImSure)

        btnCancelarDeleteTwoStepsAuth.setOnClickListener {
            root.findViewById<SwitchCompat>(R.id.swTwoFactorAuth).isChecked = true
            dialog.dismiss()
        }
        btnImSure.setOnClickListener {
            val newData: HashMap<String, Any> = hashMapOf(
                "twoStepAuthEnabled" to false
            )

            try {
                val user = db.collection("users").document(user.userId)
                val result = user.update(newData)
                if (!result.isSuccessful) {
                    Log.d("ERROR", result.exception?.message ?: "")
                    Toast.makeText(context, result.exception?.message, Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            } catch (e: Exception) {
                Log.d("ERROR", e.message ?: "")
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun deleteAccount(root: View){

        val btnDeleteAccount:Button = root.findViewById(R.id.btnDeleteAccount)




        btnDeleteAccount.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.modal_confirm_delete_account)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            val btnCancelarDeleteAccount:Button = dialog.findViewById(R.id.btnCancelarDeleteAccount)
            val btnConfirmDeleteAccount:Button = dialog.findViewById(R.id.btnConfirmDeleteAccount)


            btnCancelarDeleteAccount.setOnClickListener {
                dialog.dismiss()
            }
            btnConfirmDeleteAccount.setOnClickListener {
                try {
                    val userFirebase: FirebaseUser =  FirebaseAuth.getInstance().currentUser!!
                    val userFirebaseDeleted =  userFirebase.delete().addOnSuccessListener{
                        db.collection("users").document(user.userId)
                            .delete().addOnSuccessListener {
                                context?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)!!
                                    .edit().clear().apply()
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            }
                            .addOnFailureListener {
                                throw Exception(it)
                            }
                    }.addOnFailureListener {
                        throw Exception(it)
                    }

                } catch (e: Exception) {
                    Log.d("ERROR", e.message ?: "")
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
            }
            dialog.show()
        }

    }

    private fun setupNav(root:View){
        val bottomNavigationView = root.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemHome -> {
                    findNavController().navigate(R.id.action_settingsFragment_to_homeFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.placeholder -> {
                    findNavController().navigate(R.id.action_settingsFragment_to_addGateFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.itemSettings -> {
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

}