package com.books.brnbooks.books.screens

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.books.brnbooks.books.main.DashboardAdminActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.books.brnbooks.databinding.ActivityAddCategoryBinding


class AddCategoryActivity : AppCompatActivity() {

    //viewBinding
    private lateinit var binding: ActivityAddCategoryBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth


    //progress Dialog
//    private lateinit var progressDialog: ProgressDialog
      private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialise firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //initialise progress Dialog
//        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Please Wait...")
//        progressDialog.setCanceledOnTouchOutside(false)
        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)

        // handle click , go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
            startActivity(Intent(this, DashboardAdminActivity::class.java))
        }

        // handle click, begin upload category
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var category = ""

    private fun validateData() {
        //get data
        category = binding.categoryET.text.toString().trim()

        //validate data
        if(category.isEmpty()){
            Toast.makeText(this,"Enter Category",Toast.LENGTH_SHORT).show()
        }else{
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {
        // show progress
        dialog.show()

        //get timestamp
        val timestamp = System.currentTimeMillis()

        //setup data to add in firebase
        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add to firebase db: Database root > Categories > categoryId > categoryInfo
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                // added successfully
                Toast.makeText(this,"Added Successfully",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }.addOnFailureListener { e->
                // failed to add category
                Toast.makeText(this,"failed to add category due to ${e.message} ",Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
    }
}