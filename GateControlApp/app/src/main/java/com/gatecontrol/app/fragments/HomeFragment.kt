package com.gatecontrol.app.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gatecontrol.app.R
import com.gatecontrol.app.adapters.GateAdapter
import com.gatecontrol.app.models.Gate
import com.gatecontrol.app.models.User
import com.gatecontrol.app.network.AuthApiCliente
import com.gatecontrol.app.pages.auth.LoginPage
import com.google.android.gms.auth.api.Auth
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import javax.security.auth.callback.Callback

class HomeFragment : Fragment() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        setupNav(root)
        initRecycler(root)
        return root
    }

    private fun initRecycler(view: View) {
        val recyclerView:RecyclerView = view.findViewById(R.id.rvGates)
        val layoutNoGates = view.findViewById<LinearLayout>(R.id.layoutNoGates)
        lifecycleScope.launch(Dispatchers.Main) {
            val async = async { getGates() }
            val gateList: List<Gate> = async.await()
            if (gateList.isEmpty()){
                layoutNoGates.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                layoutNoGates.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = GateAdapter(gateList, userId)
            }
        }
    }

    private suspend fun getGates(): List<Gate> {
        val email: String? = context?.getSharedPreferences(
            getString(R.string.prefs_file), Context.MODE_PRIVATE
        )?.getString("email", "")

        email ?: {
            Toast.makeText(context, "Your session has expired, please log in again.", Toast.LENGTH_LONG).show()
            startActivity(Intent(requireContext(), LoginPage::class.java))
        }

        val gates:MutableList<Gate> = mutableListOf()

        try {
            val userRef = db.collection("users").whereEqualTo("email", email).get().await()
            for (doc in userRef) {
                userId = doc.id
            }
            val gatesRef = db.collection("users").document(userId).collection("gates")
                .get().await()

            var i = 0
            gatesRef.forEach { doc ->
                val gate = Gate(
                    name = doc["name"].toString(),
                    urlImage = Uri.parse(doc["urlImage"].toString()),
                    state = doc["state"].toString(),
                    wifiPassword = "",
                    wifiName = "",
                    voltage = 0
                )
                gate.gateId = doc.id
                gates.add(i, gate)
                i+=1
            }

        } catch (e: Exception) {
            Log.d("ERROR", e.message ?: "")
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
        return gates.toList()
    }


    private fun setupNav(root:View){
        val bottomNavigationView = root.findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemHome -> {
                    return@setOnItemSelectedListener true
                }
                R.id.placeholder -> {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddGateFragment(
                        gateId = null
                    ))
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