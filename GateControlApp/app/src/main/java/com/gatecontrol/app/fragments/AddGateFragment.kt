package com.gatecontrol.app.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gatecontrol.app.R
import com.gatecontrol.app.models.Gate
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class AddGateFragment : Fragment() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageRef:StorageReference = FirebaseStorage.getInstance().reference.child("images")
    private lateinit var imgSelected:ImageView
    private var urlImage:Uri?=null
    private val args:AddGateFragmentArgs by navArgs()
    private var userID:String = ""

    private val pickMedia = registerForActivityResult(PickVisualMedia()){ uri ->
        if (uri!=null){
            imgSelected.setImageURI(uri)
            urlImage=uri
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_add_gate, container, false)
        setupNav(root)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch(Dispatchers.Main) {
            val async = async {
                getUserID()
            }
            async.await()
            goModalSelectImage(view)
            if (args.gateId == null) {
                saveGate(view)
            } else {
                val txtTitleGate:TextView = view.findViewById(R.id.txtTitleGate)
                txtTitleGate.text = "Edit your gate"
                editGate(view)
            }
        }

    }
    private suspend fun getUserID(){
        try {
            val email: String? = context?.getSharedPreferences(
                getString(R.string.prefs_file), Context.MODE_PRIVATE
            )?.getString("email", "")

            email ?: return

            val userRef = db.collection("users").whereEqualTo("email", email).get().await()
            for (doc in userRef) {
                userID = doc.id
            }
        } catch (e:Exception){
            Log.d("ERROR", e.toString() ?: "")
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }

    }

    private fun editGate(view: View) {
         db.collection("users").document(userID).collection("gates")
            .document(args.gateId!!).get().addOnSuccessListener {
                 val txtNameGate:EditText = view.findViewById(R.id.txtNameGate)
                 val txtWifiName:EditText = view.findViewById(R.id.txtWifiName)
                 val txtWifiPassword:EditText = view.findViewById(R.id.txtWifiPassword)
                 val txtVoltage:EditText = view.findViewById(R.id.txtVoltage)
                 txtNameGate.text = SpannableStringBuilder(it.data!!["name"].toString())
                 txtWifiName.text = SpannableStringBuilder(it.data!!["wifiName"].toString())
                 txtWifiPassword.text = SpannableStringBuilder(it.data!!["wifiPassword"].toString())
                 txtVoltage.text = SpannableStringBuilder(it.data!!["voltage"].toString())
                 urlImage = Uri.parse(it.data!!["urlImage"].toString())
            }.addOnFailureListener {
                 Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                 return@addOnFailureListener
             }
        saveGate(view)
    }

    private fun saveGate(view: View) {
        val btnCreateNewGate:Button = view.findViewById(R.id.btnCreateNewGate)
        btnCreateNewGate.setOnClickListener {
            val txtNameGate:EditText = view.findViewById(R.id.txtNameGate)
            val txtWifiName:EditText = view.findViewById(R.id.txtWifiName)
            val txtWifiPassword:EditText = view.findViewById(R.id.txtWifiPassword)
            val txtVoltage:EditText = view.findViewById(R.id.txtVoltage)
            if (txtVoltage.text.trim().isEmpty()){
                Toast.makeText(context, "Voltage cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (urlImage!=null){
                val gate = Gate(
                    name = txtNameGate.text.toString(),
                    wifiName = txtWifiName.text.toString(),
                    wifiPassword = txtWifiPassword.text.toString(),
                    voltage = txtVoltage.text.toString().toInt(),
                    urlImage = urlImage!!
                )
                if (gate.validateGate()!=null){
                    Toast.makeText(context, gate.validateGate(), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                saveImageToFirebase(gate)
            } else {
                Toast.makeText(context, "You must choose an image", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }
    }

    private fun saveImageToFirebase(gate: Gate) {

            val riversRef = storageRef.child("images/${urlImage!!.lastPathSegment}")
            val uploadTask:UploadTask= riversRef.putFile(urlImage!!);

            uploadTask.addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }.addOnSuccessListener {
                riversRef.downloadUrl.addOnSuccessListener {
                    lifecycleScope.launch(Dispatchers.Main) {
                        gate.urlImage = it
                        saveGateToFirestore(gate)
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }

            }
    }

    private fun saveGateToFirestore(gate: Gate){

        try {
            if (args.gateId==null){
                db.collection("users").document(userID).collection("gates")
                    .add(gate).addOnSuccessListener {
                        Toast.makeText(context, "Gate created", Toast.LENGTH_LONG).show()
                        findNavController().navigate(AddGateFragmentDirections.actionAddGateFragmentToHomeFragment())
                    }.addOnFailureListener {
                        throw Exception(it)
                    }
            } else {
                val data = HashMap<String, Any>()
                data["name"] = gate.name
                data["wifiName"] = gate.wifiName!!
                data["wifiPassword"] = gate.wifiPassword!!
                data["voltage"] = gate.voltage!!
                data["urlImage"] = gate.urlImage.toString()

                db.collection("users").document(userID).collection("gates")
                    .document(args.gateId!!).update(data).addOnSuccessListener {
                        Toast.makeText(context, "Gate Updated", Toast.LENGTH_LONG).show()
                        findNavController().navigate(AddGateFragmentDirections.actionAddGateFragmentToHomeFragment())
                    }.addOnFailureListener {
                        throw Exception(it)
                    }
            }

        } catch (e: Exception) {
            Log.d("ERROR", e.toString() ?: "")
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun goModalSelectImage(view: View) {
        val btnGoModalSelectImage: ImageButton = view.findViewById(R.id.btnGoModalSelectImage)

        btnGoModalSelectImage.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.modal_select_image_gate)
            val btnSaveGateImage:Button = dialog.findViewById(R.id.btnSaveGateImage)
            imgSelected = dialog.findViewById(R.id.selectedGateImage)
            if (urlImage!=null){
                Picasso.get().load(urlImage).into(imgSelected)
            }
            btnSaveGateImage.setOnClickListener {
                if (urlImage!=null){
                    val icCheck:ImageView = view.findViewById(R.id.icCheck)
                    icCheck.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            }
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            pickPhoto(dialog)
            dialog.show()
        }
    }

    private fun pickPhoto(dialog: Dialog) {
        val btnGoModalSelectImage:ImageButton = dialog.findViewById(R.id.btnGoModalSelectImage)
        btnGoModalSelectImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
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