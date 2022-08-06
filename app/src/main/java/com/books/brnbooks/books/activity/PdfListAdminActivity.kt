package com.books.brnbooks


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.books.brnbooks.books.adapter.AdapterPdfAdmin
import com.books.brnbooks.books.model.ModelPdf
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.books.brnbooks.databinding.ActivityPdfListAdminBinding

class PdfListAdminActivity : AppCompatActivity() {

    // viewBinding
    private lateinit var binding:ActivityPdfListAdminBinding

    // arrayList to hold books
    private lateinit var pdfArrayList: ArrayList<ModelPdf>// model pdf

    // adapter
    private lateinit var adapterPdfAdmin: AdapterPdfAdmin



    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //category Id, title
    private var categoryId = ""
    private var category = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get data from intent, that we passed from adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        // set pdf category
        binding.subtitleTv.text = category






        // load pdf/books
        loadPdfList()

        //search
        binding.searchEt.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    try {
                        adapterPdfAdmin.filter.filter(s)
                    }catch (e : Exception){
                        Toast.makeText(this@PdfListAdminActivity,"due to ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            }
        )

        // handle click, go back
        binding.nibBack.setOnClickListener {
            onBackPressed()
        }

    }

    private fun loadPdfList() {
        Log.d(TAG, "loadPdfList: Loading Pdf Categories")
        // init arrayList
        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(category) // categoryId
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear list before start adding data into it
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        // get data
                        val model = ds.getValue(ModelPdf::class.java)
                        // add to list

                        if(model!=null){
                            pdfArrayList.add(model)
                            Log.d(TAG,"onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    // setup adapter
                    adapterPdfAdmin = AdapterPdfAdmin(this@PdfListAdminActivity,pdfArrayList)
                    binding.booksRV.adapter = adapterPdfAdmin

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }
}