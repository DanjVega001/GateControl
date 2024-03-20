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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gatecontrol.app.R
import com.gatecontrol.app.network.AuthApiCliente
import com.gatecontrol.app.network.AuthApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ActivatePhoneFragment : Fragment() {

    private val service:AuthApiService = AuthApiCliente.getInstance()
    private val argsFragment:ActivatePhoneFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_activate_phone, container, false)
        sendPhoneNumber(root)
        cancel(root)
        return root
    }

    private fun cancel(root: View){
        val btnCancel:Button = root.findViewById(R.id.btnCancelarActivatePhone)
        btnCancel.setOnClickListener {
            findNavController().navigate(ActivatePhoneFragmentDirections.actionActivatePhoneFragmentToSettingsFragment(
                isCancel = true
            ))
        }
    }

    private fun sendPhoneNumber(root: View) {
        val btnSendPhoneNumber:Button = root.findViewById(R.id.btnSendPhoneNumber)

        btnSendPhoneNumber.setOnClickListener {
            val txtPhoneNumber:EditText = root.findViewById(R.id.txtPhoneNumber)
            if (txtPhoneNumber.text.trim().isEmpty()) {
                Toast.makeText(context, "Phone number cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (txtPhoneNumber.text.trim().length != 10){
                Toast.makeText(context, "Invalid phone number", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            lifecycleScope.launch(Dispatchers.IO){
                val deferred =
                    async { service.sendNumberPhone(txtPhoneNumber.text.toString(), false) }

                val response = deferred.await()

                withContext(Dispatchers.Main){
                    if (response.isSuccessful) {
                        val data = response.body()
                        Toast.makeText(context, data?.get("message").toString(), Toast.LENGTH_LONG).show()
                        findNavController().navigate(ActivatePhoneFragmentDirections.actionActivatePhoneFragmentToVerifyCodeFragment(
                            code = (data?.get("code") as Double).toInt(),
                            userId = argsFragment.userId,
                            phone = txtPhoneNumber.text.toString().toLong()
                        ))
                    } else {
                        Log.d("Error", response.toString())
                        Toast.makeText(context, "Error "+response.message(), Toast.LENGTH_LONG).show()
                    }
                }

            }

        }
    }




}