

package com.books.brnbooks.books.screens


import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.books.brnbooks.R
import com.books.brnbooks.books.main.DashboardAdminActivity
import com.books.brnbooks.books.main.DashboardUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.books.brnbooks.databinding.ActivityLoginBinding



class LoginActivity : AppCompatActivity() {
    //viewBinding
    private lateinit var binding: ActivityLoginBinding

    //firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth


    //progress Dialog
//    private lateinit var progressDialog: ProgressDialog

    private lateinit var dialog:Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialise firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //initialise progress Dialog will show while Login Account | Register User
        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)

//        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Please Wait...")
//        progressDialog.setCanceledOnTouchOutside(false)

        // handle click no Account, Go to register Screen
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            /*
           * 1) Input Data
           * 2) validate data
           * 3) Login - Firebase Auth
           * 4) Check userType - firebase auth
           *    If user -> move to user dashboard
           *    If admin-> move to admin dashboard*/


            validateData()
        }

        binding.forgotTv.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }

    }



    private var email = ""
    private var password = ""

    private fun validateData() {
        //Input Data
        email = binding.emailET.text.toString().trim()
        password = binding.passwordET.text.toString().trim()

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // invalid email adress
            Toast.makeText(this, "Invalid! Email Adress", Toast.LENGTH_SHORT).show()
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {

        //        3) Login - Firebase Auth

        // show progress
//        progressDialog.setMessage("Logging In...")
//        progressDialog.show()

        dialog.setContentView(R.layout.dialog_logging_in)
        dialog.show()


        firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            // login success
            checkUser()
        }.addOnFailureListener { e ->
            // login failed
//            progressDialog.dismiss()
            dialog.dismiss()
            Toast.makeText(this, "Login Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkUser() {
       /* * 4) Check userType - firebase auth
        *    If user -> move to user dashboard
        *    If admin-> move to admin dashboard*/

        dialog.setContentView(R.layout.dialog_checking_user)

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    // get user type user/admin
                    val userType = snapshot.child("userType").value
                    if (userType == "user"){
                        // it's simple open user dashboard
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    }else if(userType == "admin"){
                        // it's simple open admin dashboard
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}