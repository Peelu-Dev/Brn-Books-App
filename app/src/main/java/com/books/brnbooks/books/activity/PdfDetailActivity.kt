package com.books.brnbooks.books.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.books.brnbooks.R
import com.books.brnbooks.books.main.Constants
import com.books.brnbooks.books.main.MyApplication
import com.books.brnbooks.breath.BreathActivity
import com.books.brnbooks.databinding.ActivityPdfDetailBinding
import com.books.brnbooks.notes.activity.MainActivity2
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {
    // viewBinding
    private lateinit var binding: ActivityPdfDetailBinding

    private companion object{
        const val TAG = "BOOK_DETAILS_TAG"
    }

    // book id
    private var bookId = ""

    // get from firebase
    private var bookTitle = ""
    private var bookUrl = ""


    //progress Dialog
//    private lateinit var progressDialog: ProgressDialog
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // get book id to edit book info
        bookId = intent.getStringExtra("bookId")!!


        // increment book view count, whenever this page starts
        MyApplication.incrementBookViewCount(bookId)


        //initialise progress Dialog will show while Login Account | Register User
//        progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Please Wait...")
//        progressDialog.setCanceledOnTouchOutside(false)
        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)


        loadBookDetails()

        // handle click, go back
        binding.nibBack.setOnClickListener {
            onBackPressed()
        }

        binding.breatheBtn.setOnClickListener {
            startActivity(Intent(this,BreathActivity::class.java))
        }

        binding.noteBookBtn.setOnClickListener {
            startActivity(Intent(this,MainActivity2::class.java))
        }

        // handle click , open pdf activity
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this,PdfViewActivity::class.java)
            intent.putExtra("bookId",bookId)
            startActivity(intent)
        }

//        handle click , download book
        binding.downloadBookBtn.setOnClickListener {
            // lets check WRITE_EXTERNAL_STORAGE first, if granted download book, if not granted request permission
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"onCreate: Storage permission is already granted")
                downLoadBook()
            }else{
                Log.d(TAG,"onCreate: Storage permission was not granted, lets request it")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private val requestStoragePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted:Boolean->
        // lets check , if granted or not
        if (isGranted){
            Log.d(TAG,"onCreate: Storage permission is granted")
        }else{
            Log.d(TAG,"onCreate: Storage permission is denied")
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
        }
    }

    private fun downLoadBook(){
        Log.d(TAG,"downloadBook: downloading book")
        dialog.setContentView(R.layout.dialog_downloading_book)
        dialog.show()

        // lets download books from firebase storage using url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener {bytes->
                Log.d(TAG,"downloadBook : Book downloaded")
                saveToDownloadFolder(bytes)
            }.addOnFailureListener {e->
                dialog.dismiss()
                Log.d(TAG,"downloadBook : failed to download book due to ${e.message}")
                Toast.makeText(this,"Failed to download book due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadFolder(bytes: ByteArray?) {
        Log.d(TAG,"saveToDownloadsFolder: saving downloaded book")

        val nameWithExtension = "${System.currentTimeMillis()}.pdf"

        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdir() // create folder if not exists

            val filePath = downloadsFolder.path + "/" + nameWithExtension

            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()

            Toast.makeText(this,"saved to Downloads Folder",Toast.LENGTH_SHORT).show()

            Log.d(TAG,"saveToDownloadsFolder: saved to downloads folder")
            dialog.dismiss()
            incrementDownloadsCount()
        }catch (e:Exception){
            dialog.dismiss()
            Log.d(TAG,"saveToDownloadsFolder: failed to save pdf due to ${e.message}")
            Toast.makeText(this,"failed to save pdf due to ${e.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadsCount() {
        // increment downloads count to firebase db
        Log.d(TAG,"incrementDownloadCount:")

        // step 1) get previous download count
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get downloads count
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"

                    if (downloadsCount =="" || downloadsCount == "null"){
                        downloadsCount = "0"
                    }

                    // convert to long and increment 1
                    val newDownloadsCount = downloadsCount.toLong() + 1

                    //setup data to update in db
                    //setup data to update in db
                    val hashMap = HashMap<String,Any>()
                    hashMap["downloadsCount"] = newDownloadsCount

                        // Step 2) Update new increment counts to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG,"onDataChange: Downloads count incremented")
                        }.addOnFailureListener {e->
                            Log.d(TAG,"onDataChange: Failed to increment due to ${e.message}")
                        }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


    private fun loadBookDetails() {
        // Books > BookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    // format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())



                    // load pdf thumbnail, pages count
                    MyApplication.loadPdfFromUrlSinglePage(""+bookUrl,""+bookTitle,binding.pdfView,binding.progressBar,binding.pagesTv)

                    //load pdf size
                    MyApplication.loadPdfSize(""+bookUrl,""+bookTitle,binding.sizeTv)

                    // set data
                    binding.titleTv.text = bookTitle
                    binding.categoryTv.text = categoryId
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date
                    binding.categoryTv.text = categoryId

                    // load pdf category
//                    MyApplication.loadCategory(categoryId,binding.categoryTv)

                    Log.d(TAG,"bookDetails: category: $categoryId")


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}