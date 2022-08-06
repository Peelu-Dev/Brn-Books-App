

package com.books.brnbooks.books.screens



import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.books.brnbooks.R
import com.books.brnbooks.books.main.DashboardAdminActivity
import com.books.brnbooks.books.main.DashboardUserActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class SplashScreen : AppCompatActivity() {
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
//    private lateinit var progressDialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUser()
        },2000)
    }

    private fun checkUser() {
        //get current user if logged in or not
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser==null){
            //user not logged in go to main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
            // user logged in, check user type same in login screen

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get user type user/admin
                        val userType = snapshot.child("userType").value
                        if (userType == "user"){
                            // it's simple open user dashboard
                            startActivity(Intent(this@SplashScreen, DashboardUserActivity::class.java))
                            finish()
                        }else if(userType == "admin"){
                            // it's simple open admin dashboard
                            startActivity(Intent(this@SplashScreen, DashboardAdminActivity::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}