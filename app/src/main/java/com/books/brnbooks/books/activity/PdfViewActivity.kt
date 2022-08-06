package com.books.brnbooks.books.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.books.brnbooks.books.main.Constants
import com.books.brnbooks.books.screens.DonateActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.books.brnbooks.databinding.ActivityPdfViewBinding


class PdfViewActivity : AppCompatActivity() {

    // viewBinding
    private lateinit var binding: ActivityPdfViewBinding

    // book id
    private var bookId = ""

    private companion object {
        const val TAG = "PDF_VIEW_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get book id to edit book info
        bookId = intent.getStringExtra("bookId")!!

        loadBookDetails()

        binding.donate.setOnClickListener {
            startActivity(Intent(this,DonateActivity::class.java))
        }

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get pdf url from db")
        // Database reference to get book details
        // Step 1) get book url from book id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book url
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL $pdfUrl")

                    // load pdf using url from firebase storage
                    loadBookFromUrl("$pdfUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadBookFromUrl: Get pdf  from firebase storage using url")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadBookFromUrl: pdf got  from firebase storage using url")

                // load pdf
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        // set current and total pages  in toolbar subtitle
                        val currentPage = page + 1
                        val pageNo =  "$currentPage/$pageCount"
                        binding.toolbarSubtitleTv.text = pageNo
                        Log.d(TAG, "loadBookFromUrl: $pageNo")

                    }.onError { t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }.onPageError { _, t ->
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }.addOnFailureListener { e ->
                Log.d(TAG, "loadBookFromUrl: failed due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }
    }
}