package com.gatecontrol.app.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gatecontrol.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class AddGateFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_gate, container, false)
        setupNav(root)
        return root
    }

    private fun setupNav(root:View){
        val bottomNavigationView = root.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemHome -> {
                    findNavController().navigate(R.id.action_addGateFragment_to_homeFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.placeholder -> {
                    return@setOnItemSelectedListener true
                }
                R.id.itemSettings -> {
                    findNavController().navigate(R.id.action_addGateFragment_to_settingsFragment)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

}