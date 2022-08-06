package com.books.brnbooks.books.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.books.brnbooks.R
import com.books.brnbooks.books.main.DashboardAdminActivity
import com.books.brnbooks.books.model.ModelCategory
import com.books.brnbooks.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class PdfAddActivity : AppCompatActivity() {
    //viewBinding
    private lateinit var binding: ActivityPdfAddBinding

    // firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog to show while uploading pdf
//    private lateinit var progressDialog: ProgressDialog
    private lateinit var dialog: Dialog

    // category array list to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    //array list to hold pdf categories
    private var pdfUri:Uri? = null

    //Tag
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        // setup progress dialog
        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
            startActivity(Intent(this, DashboardAdminActivity::class.java))
        }

        //handle click, show categories, pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        //handle click, pick pdf intent
        binding.attachPdfBtn.setOnClickListener {
            pdfPickIntent()
        }

        //handle click, start uploading pdf / book
        binding.uploadBtn.setOnClickListener {
            // step1: validate data
            // step2: upload pdf to firebase storage
            // step3: get uri of uploaded pdf
            // step4: uploaded pdf info to firebase db

            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        // step1: validate data
        Log.d(TAG,"validateData : Validate Data")

        //get data
        title = binding.titleET.text.toString().trim()
        description = binding.descriptionET.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        //validate data
        if(title.isEmpty()){
            Toast.makeText(this,"Enter Title...",Toast.LENGTH_SHORT).show()
        }else if(description.isEmpty()){
            Toast.makeText(this,"Enter Description...",Toast.LENGTH_SHORT).show()
        }else if(category.isEmpty()){
            Toast.makeText(this,"Pick Category...",Toast.LENGTH_SHORT).show()
        }else  if(pdfUri == null){
            Toast.makeText(this,"Pick PDF...",Toast.LENGTH_SHORT).show()
        }
        else{
            //data validated, begin uploading
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        // Step2 : upload Pdf To Firebase Storage
        Log.d(TAG,"uploadPdfToStorage: uploading to firebase")

        //show progress dialog
        dialog.setContentView(R.layout.dialog_uploading_pdf)
        dialog.show()

        //timestamp
        val timestamp = System.currentTimeMillis()

        //path of pdf in firebase storage
        val filePathAndName = "Books/$timestamp"

        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot->
                Log.d(TAG,"uploadPdfToStorage: PDF uploaded now getting URL...")
                //Step3 : Get url of uploaded pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl,timestamp)
            }.addOnFailureListener{e->
                Log.d(TAG,"uploadPdfToStorage: failed to upload pdf due to ${e.message}...")
                dialog.dismiss()
                Toast.makeText(this,"failed to upload pdf due to ${e.message}...",Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        // step4: uploaded pdf info to firebase db
        Log.d(TAG,"uploadPdfInfoToDb: uploading to db")


        //uid of current user
        val uid = firebaseAuth.uid

        //setup data to upload
        val hashMap:HashMap<String,Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = ""+ title
        hashMap["description"] = "" + description
        hashMap["categoryId"] = "" + selectedCategoryId
        hashMap["url"] = ""+ uploadedPdfUrl
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        // db reference DB > Books > BookId > (Book Info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"uploadInfoToDb: upload to db")
                dialog.dismiss()
                Toast.makeText(this,"Uploaded...",Toast.LENGTH_SHORT).show()
                pdfUri = null
            }.addOnFailureListener { e->
                Log.d(TAG,"uploadInfoToDb: failed to upload due to ${e.message}")
                dialog.dismiss()
                Toast.makeText(this,"failed to upload due to ${e.message}...",Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Loading Pdf Categories")
        // init arrayList
        categoryArrayList = ArrayList()

        //db reference to load categories PDF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before adding data
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)
                    // add to array list
                    categoryArrayList.add(model!!)
                    Log.d(TAG,"onDataChange: ${model.category}")

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog(){
        Log.d(TAG,"categoryPickDialog : showing pdf category pick dialog ")
        // get string array of categories from array list
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){_,which->
                // handle item click
                //get clicked items
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].category
                // set category to textview
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG,"categoryPickDialog: Selected Category Id: $selectedCategoryId ")
                Log.d(TAG,"categoryPickDialog: Selected Category Title: $selectedCategoryTitle ")
            }.show()

    }

    private fun pdfPickIntent(){
        Log.d(TAG,"pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }
    private val pdfActivityResultLauncher =
        registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            if(result.resultCode == RESULT_OK){
                Log.d(TAG,"Pdf Picked")
                pdfUri = result.data!!.data
            }else {
                Log.d(TAG,"Pdf Pick Canceled")
                Toast.makeText(this,"Cancelled",Toast.LENGTH_SHORT).show()
            }
        }
    )
}
