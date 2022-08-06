

package com.books.brnbooks.books.screens



import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.books.brnbooks.R
import com.books.brnbooks.books.main.DashboardUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.books.brnbooks.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress Dialog
      private  lateinit var dialog: Dialog
//      private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initialise firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //initialise progress Dialog will show while creating Account | Register User
        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)
//        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Please Wait...")
//        progressDialog.setCanceledOnTouchOutside(false)


        // handle back button click
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // handle click, begin register
        binding.registerBtn.setOnClickListener {
            /*
            * 1) Input Data
            * 2) validate data
            * 3) Create Account - Firebase Auth
            * 4) Save User Info - Firebase Realtime database*/

            validateData()
        }



    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        // 1) Input Data
        name = binding.nameET.text.toString().trim()
        email = binding.emailET.text.toString().trim()
        password = binding.passwordET.text.toString().trim()
        val cpassword = binding.cpasswordET.text.toString().trim()

        // validate data
        if(name.isEmpty()){
            // empty name
            Toast.makeText(this,"Enter your name",Toast.LENGTH_SHORT).show()
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            // invalid email adress
            Toast.makeText(this,"Invalid! Email Adress",Toast.LENGTH_SHORT).show()
        }else if(password.isEmpty()){
            Toast.makeText(this,"Enter your password",Toast.LENGTH_SHORT).show()
        }else if(cpassword.isEmpty()){
            Toast.makeText(this,"Confirm Password",Toast.LENGTH_SHORT).show()
        }else if(password != cpassword){
            Toast.makeText(this,"password doesn't match",Toast.LENGTH_SHORT).show()
        }else{
            createUserAccount()
        }
    }

    private fun createUserAccount() {

//        3) Create Account - Firebase Auth

        // show progress
        dialog.setContentView(R.layout.dialog_creating_account)
        dialog.show()
//        progressDialog.setMessage("Creating Account...")
//        progressDialog.show()

        // create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {
            // account created now add user info in db
            updateUserInfo()
        }.addOnFailureListener { e->
            dialog.dismiss()
            Toast.makeText(this,"Failed creating account due to ${e.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserInfo() {
//        4) Save User Info - Firebase Realtime database

        dialog.setContentView(R.layout.dialog_user_info)

        // timestamp
        val timestamp = System.currentTimeMillis()

        //get user uid, since user is register so we can get it now
        val uid = firebaseAuth.uid

        //setup data to add in db
        val hashMap:HashMap<String,Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] ="" // will do it later
        hashMap["userType"] = "user" // possible values are user/admin, will change manually in firebase db
        hashMap["timestamp"] = timestamp

        //set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(this,"Account Created...",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }.addOnFailureListener {e->
                // failed adding data to db
                dialog.dismiss()
                Toast.makeText(this,"Failed saving data due to ${e.message}",Toast.LENGTH_SHORT).show()
            }


    }
}