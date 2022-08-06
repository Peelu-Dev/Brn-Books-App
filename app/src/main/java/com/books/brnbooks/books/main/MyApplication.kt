package com.books.brnbooks.books.main

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.books.brnbooks.R
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object{

        fun formatTimeStamp(timestamp:Long):String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return  DateFormat.format("dd/MM/yyyy",cal).toString()
        }



        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv:TextView?
        ){
            val tag = "PDF_THUMBNAIL_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes->
                    Log.d(tag,"loadPdfSize: got metadata")
                    Log.d(tag,"loadPdfSize: Size bytes $bytes")

                   // Set to pdf View
                    pdfView.fromBytes(bytes)
                        .pages(0) // show first page only
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(tag,"loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { _, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(tag,"loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages->
                            Log.d(tag,"loadPdfFromUrlSinglePage: Pages: $nbPages")
                            // pdf loaded , we can set page count, pdf thumbnail
                            progressBar.visibility = View.INVISIBLE

                            // if pages params are not null then set page numbers
                            if (pagesTv != null){
                                pagesTv.text = "$nbPages"
                            }
                        }
                        .load()

                }.addOnFailureListener {e->
                    Log.d(tag,"loadPdfFromUrlSinglePage: Failed to get metadata due to ${e.message}")
                }
        }

        // function to get pdf size
        fun loadPdfSize(pdfUrl:String,pdfTitle:String,sizeTv:TextView){
            val tag = "PDF_SIZE_TAG"
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData->
                    Log.d(tag,"loadPdfSize: got metadata")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(tag,"loadPdfSize: Size bytes $bytes")

                    // convert bytes to kb/mb
                    val kb = bytes/1024
                    val mb = kb/1024
                    val setText = "${String.format("%.2f",mb)} MB"
                    if(mb>=1){
                        sizeTv.text = setText
                    }else if(kb>=1){
                        sizeTv.text = setText
                    }else{
                        sizeTv.text = setText
                    }
                }.addOnFailureListener {e->
                    Log.d(tag,"loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }


        fun loadCategory(categoryId:String,categoryTv:TextView){
            // load category using categoryId from Firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get category
                        val category ="${snapshot.child("category").value}"



                        categoryTv.text = category

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }

        fun deleteBook(context: Context,bookId:String,bookUrl:String,bookTitle:String) {
         // param details
//            1) Context, used when require, eg. for progressDialog , Toast
//            2) bookId, to delete book from db
//            3) bookUrl, to delete book from firebase
//            2) bookTitle, to show in dialog etc


            val tag = "DELETE_BOOK_TAG"

            Log.d(tag,"deleteBook: deleting...")

            //progress Dialog
            val dialog = Dialog(context)
//            val progressDialog = ProgressDialog(context)
            dialog.setContentView(R.layout.dialog_please_wait)
            Toast.makeText(context,"deleting $bookTitle",Toast.LENGTH_SHORT).show()
//            progressDialog.setMessage("deleting $bookTitle")
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            Log.d(tag, "deleteBook: deleting from firebase storage")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(tag,"deleteBook: deleting from storage")
                    Log.d(tag,"deleteBook: deleting from db now...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            dialog.dismiss()
                            Toast.makeText(context,"Successfully Deleted...",Toast.LENGTH_SHORT).show()
                            Log.d(tag,"deleteBook: Deleted from db too... ")
                        }.addOnFailureListener {e->
                            dialog.dismiss()
                            Log.d(tag,"deleteBook: Failed to delete from Firebase db due ${e.message} ")
                            Toast.makeText(context,"Failed to delete from firebase db due to ${e.message}",Toast.LENGTH_SHORT).show()
                        }
                }.addOnFailureListener {e->
                    dialog.dismiss()
                    Log.d(tag,"deleteBook: Failed to delete from Firebase storage due ${e.message} ")
                    Toast.makeText(context,"Failed to delete from firebase storage due to ${e.message}",Toast.LENGTH_SHORT).show()
                }

        }


        fun incrementBookViewCount(bookId: String){
//            1) Get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount =="" || viewsCount == "null"){
                            viewsCount = "0"

                        }

                        // 2) increment viewsCount
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in db
                        val hashMap = HashMap<String,Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

    }

}