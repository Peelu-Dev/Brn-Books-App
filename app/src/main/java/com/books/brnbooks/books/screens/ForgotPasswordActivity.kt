package com.books.brnbooks.books.screens

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.books.brnbooks.R
import com.google.firebase.auth.FirebaseAuth
import com.books.brnbooks.databinding.ActivityForgotPasswordBinding


class ForgotPasswordActivity : AppCompatActivity() {
    //viewBinding
    private lateinit var binding: ActivityForgotPasswordBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth


    //progress Dialog
//    private lateinit var progressDialog: ProgressDialog
      private lateinit var dialog:Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialise firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //initialise progress Dialog will show while Login Account | Register User
//        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Please Wait...")
//        progressDialog.setCanceledOnTouchOutside(false)
        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)

        // handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        // handle click, begin password recovery process
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private  var email = ""
    private fun validateData() {
        //Input Data
        email = binding.emailET.text.toString().trim()

        // validate data
        if (email.isEmpty()) {
            Toast.makeText(this, "Enter your email...", Toast.LENGTH_SHORT).show()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // invalid email adress
            Toast.makeText(this, "Invalid! Email pattern...", Toast.LENGTH_SHORT).show()
        }else{
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        // show progress
        dialog.setContentView(R.layout.dialog_please_wait)
//        dialog.setTitle("sending password reset instructions to $email")
//        progressDialog.setMessage("sending password reset instructions to $email")
        dialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(this,"Instructions sent to \n $email",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e->
                dialog.dismiss()
                Toast.makeText(this,"Failed to send instructions due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }
}