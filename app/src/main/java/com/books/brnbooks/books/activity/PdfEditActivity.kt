package com.books.brnbooks.books.activity

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.books.brnbooks.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.books.brnbooks.databinding.ActivityPdfEditBinding

class PdfEditActivity : AppCompatActivity() {
    // ViewBinding
    private lateinit var binding:ActivityPdfEditBinding

    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }


    // progress Dialog
//    private lateinit var progressDialog: ProgressDialog
    private lateinit var dialog: Dialog

    // array list to hold category titles
     private lateinit var categoryTitleArrayList:ArrayList<String>

    // array list to hold category id
    private lateinit var categoryIdArrayList:ArrayList<String>

    //         book Id get from intent started from AdapterPdfAdmin
    private var bookId = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // get book id to edit book info
        bookId = intent.getStringExtra("bookId")!!

//        setup progress dialog
        dialog = Dialog(this)
        dialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        // handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // handle click, pick category
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        // handle click, begin update
        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG,"loadBookInfo: loading book info")
                val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    //set to views
                    binding.titleET.setText(title)
                    binding.descriptionET.setText(description)

                    // load book category info using categoryId
                    Log.d(TAG,"onDataChange:Loading book category info...")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // get category
                                val category = snapshot.child("category").value

                                // set to textView
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


    private var title = ""
    private var description = ""

    private fun validateData() {
        // step1: validate data
        Log.d(TAG,"validateData : Validate Data")

        //get data
        title = binding.titleET.text.toString().trim()
        description = binding.descriptionET.text.toString().trim()

        if(title.isEmpty()){
            Toast.makeText(this,"Enter Title...", Toast.LENGTH_SHORT).show()
        }else if(description.isEmpty()){
            Toast.makeText(this,"Enter Description...", Toast.LENGTH_SHORT).show()
        }else if(selectedCategoryId.isEmpty()){
            Toast.makeText(this,"Pick Category...", Toast.LENGTH_SHORT).show()
        }else{
            updatePdf()
        }
    }

    private fun updatePdf() {

        Log.d(TAG,"updatePdf: starting updating pdf info...")

        //show progress dialog
        dialog.setContentView(R.layout.dialog_update_book_info)
        dialog.show()

        // setup data to update to db, spellings of keys must be same as in firebase
        val hashMap:HashMap<String, Any> =  HashMap()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        // start updating pdf
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"updatePdf: updated Successfully")
                dialog.dismiss()
                Toast.makeText(this,"Updated Successfully",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {e->
                Log.d(TAG,"updatePdf: Failed to update due to ${e.message}")
                dialog.dismiss()
                Toast.makeText(this,"Failed update due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {
        Log.d(TAG,"categoryPickDialog : showing pdf category pick dialog ")
        // get string array of categories from array list
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)

        for (i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){_,position->
                // handle item click
                //get clicked items
                selectedCategoryTitle = categoryTitleArrayList[position]
                selectedCategoryId = categoryIdArrayList[position]

                // set category to textview
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG,"categoryPickDialog: Selected Category Id: $selectedCategoryId ")
                Log.d(TAG,"categoryPickDialog: Selected Category Title: $selectedCategoryTitle ")
            }.show()
    }

    private fun loadCategories() {
        Log.d(TAG,"loadCategories: Loading Categories...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        //get all categories from firebase database... Firebase DB > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before starting adding data into it
                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()

                for (ds in snapshot.children){
                    // get data as model
//                    val model = ds.getValue(ModelCategory::class.java)
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG,"onDataChange: CategoryId $id")
                    Log.d(TAG,"onDataChange: Category $category")
                }
//
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}