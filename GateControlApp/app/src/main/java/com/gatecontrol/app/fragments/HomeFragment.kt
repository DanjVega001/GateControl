package com.gatecontrol.app.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.gatecontrol.app.R
import com.gatecontrol.app.models.User
import com.gatecontrol.app.network.AuthApiCliente
import com.google.android.gms.auth.api.Auth
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import javax.security.auth.callback.Callback

class HomeFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        setupNav(root)

        return root
    }


    private fun setupNav(root:View){
        val bottomNavigationView = root.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemHome -> {
                    return@setOnItemSelectedListener true
                }
                R.id.placeholder -> {
                    findNavController().navigate(R.id.action_homeFragment_to_addGateFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.itemSettings -> {
                    findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

}