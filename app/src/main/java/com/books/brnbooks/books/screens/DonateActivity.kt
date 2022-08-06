package com.books.brnbooks.books.screens



import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.books.brnbooks.databinding.ActivityDonateBinding




class DonateActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDonateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDonateBinding.inflate(layoutInflater)
    setContentView(binding.root)


        binding.nibBack.setOnClickListener {
            onBackPressed()
        }

        binding.whatsapp.setOnClickListener {

            try {
                val headerReceiver = "UPI/Debit Card Payment Link.\n" // Replace with your message.
                val bodyMessageFormal = "\nPlease send your  UPI/Debit card payment link to support BRN Books." // Replace with your message.
                val whatsappContain = headerReceiver + bodyMessageFormal
                val trimToNumber = "+919174151700" //10 digit number
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://wa.me/$trimToNumber/?text=$whatsappContain")
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.email.setOnClickListener {


            val to = "brnbooks8@gmail.com"
            val subject = "UPI/Debit Card Payment Link"
            val body = "Please send your  UPI/Debit card payment link to support BRN Books."
            val mailTo = "mailto:" + to +
                    "?&subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body)
            val emailIntent = Intent(Intent.ACTION_VIEW)
            emailIntent.data = Uri.parse(mailTo)
            startActivity(emailIntent)
        }

        binding.coffee.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/palashraghu")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.buymeacoffee.com/palashraghu")))
            }
        }

    }


}



