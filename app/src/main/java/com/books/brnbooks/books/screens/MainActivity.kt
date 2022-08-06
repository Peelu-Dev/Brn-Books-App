package com.books.brnbooks.books.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.books.brnbooks.books.main.DashboardUserActivity
import com.books.brnbooks.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    // View Binding
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // handle click,login
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

        binding.donateBtn.setOnClickListener {
            startActivity(Intent(this,DonateActivity::class.java))
        }

        // handle click, skip and continue to main screen
        binding.skipBtn.setOnClickListener {
            startActivity(Intent(this, DashboardUserActivity::class.java))
        }

    }
}