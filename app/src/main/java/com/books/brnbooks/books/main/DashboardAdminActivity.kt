package com.books.brnbooks.books.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.books.brnbooks.books.activity.PdfAddActivity
import com.books.brnbooks.books.adapter.AdapterCategory
import com.books.brnbooks.books.model.ModelCategory
import com.books.brnbooks.books.screens.AddCategoryActivity
import com.books.brnbooks.books.screens.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.books.brnbooks.databinding.ActivityDashboardAdminBinding


class DashboardAdminActivity : AppCompatActivity() {
    //viewBinding
    private lateinit var binding: ActivityDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arrayList to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener(
            object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    try {
                        adapterCategory.filter.filter(s)
                    }catch (e : Exception){
                        Toast.makeText(this@DashboardAdminActivity,"due to ${e.message}",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            }
        )


        // handle click,logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        binding.nibBack.setOnClickListener {
            onBackPressed()
        }

        // handle click, start Add Category Activity
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, AddCategoryActivity::class.java))
            finish()
        }

        // handle click, start Add PDF Activity
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
            finish()
        }
    }

    private fun loadCategories() {
        // init arrayList
        categoryArrayList = ArrayList()

        //get all categories from firebase database... Firebase DB > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before starting adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    // get data as model
                    val model = ds.getValue(ModelCategory::class.java)

                    // add to arrayList
                    if (model != null) {
                        categoryArrayList.add(model)
                    }
                }
                // setup adapter
                adapterCategory = AdapterCategory(this@DashboardAdminActivity,categoryArrayList)
                // set adapter to recycler View
                binding.categoriesRV.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser==null){
            // not logged in , go to main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }else{
            //  logged in , get and show user info
            val email = firebaseUser.email
            // set to textview to toolbar
            binding.subtitleTv.text = email
        }
    }
}