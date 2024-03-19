package com.gatecontrol.app.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gatecontrol.app.R
import com.google.firebase.firestore.FirebaseFirestore


class VerifyCodeFragment : Fragment() {

    private val args:VerifyCodeFragmentArgs by navArgs()
    private val db:FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_verify_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendCode(view)
        cancel(view)
    }

    private fun cancel(root: View){
        val btnCancel:Button = root.findViewById(R.id.btnCancelarSendCode)
        btnCancel.setOnClickListener {
            findNavController().navigate(ActivatePhoneFragmentDirections.actionActivatePhoneFragmentToSettingsFragment(
                isCancel = true
            ))
        }
    }

    private fun sendCode(view: View){
        val btnVerifyCode:Button = view.findViewById(R.id.btnVerifyCode)

        btnVerifyCode.setOnClickListener {
            val txtCode:EditText = view.findViewById(R.id.txtCode)

            if (txtCode.text.trim().isEmpty()){
                Toast.makeText(context, "Field code cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (txtCode.text.toString().toInt() != args.code){
                Toast.makeText(context, "Incorrect code", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val newData: HashMap<String, Any> = hashMapOf(
                "phone" to args.phone,
                "twoStepAuthEnabled" to true
            )

            db.collection("users").document(args.userId)
                .update(newData)
                .addOnSuccessListener {
                    findNavController().navigate(VerifyCodeFragmentDirections.actionVerifyCodeFragmentToSettingsFragment(
                        isCancel = false
                    ))
                }
                .addOnFailureListener {
                    Log.d("ERROR", it.toString())
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }

        }
    }



}