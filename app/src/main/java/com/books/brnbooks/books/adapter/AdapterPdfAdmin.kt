package com.books.brnbooks.books.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.books.brnbooks.*
import com.books.brnbooks.books.activity.PdfDetailActivity
import com.books.brnbooks.books.activity.PdfEditActivity
import com.books.brnbooks.books.filter.FilterPdfAdmin
import com.books.brnbooks.books.main.MyApplication
import com.books.brnbooks.books.model.ModelPdf
import com.books.brnbooks.databinding.RowPdfAdminBinding

class AdapterPdfAdmin:RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>,Filterable {

    //context
    private var context: Context
    // array list to hold pdfs
     var pdfArrayList:ArrayList<ModelPdf>
     private val filterList:ArrayList<ModelPdf>



    //viewBinding
    private lateinit var binding:RowPdfAdminBinding

    // filter object
    private var filter: FilterPdfAdmin? = null

    //constructor
    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        //inflate/bind the row_pdf_admin.xml
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        // get data, set data, Handle clicks etc

        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
//        val  uid = model.uid
//        val bookId = model.id  // bookId
        val  title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        // convert timestamp to dd/MM/yyyy format
        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        // set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate
        holder.categoryTv.text = categoryId

        // load further details like category, pdf from url, pdf size
//        MyApplication.loadCategory(categoryId,holder.categoryTv)

        // we don't need page number here , pass null for page number || load pdf thumbnail
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        // handle click, show dialog with options, 1) Edit Book, 2) Delete Book
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model,holder)

            // handle item click, open PdfDetailActivity
            holder.itemView.setOnClickListener {
                // intent with book id
                val intent = Intent(context, PdfDetailActivity::class.java)
                intent.putExtra("bookId",pdfId)
                context.startActivity(intent)
            }
        }
    }

    private fun moreOptionsDialog(model: ModelPdf, holder: HolderPdfAdmin) {
            // get id,url,title of the  book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        // options to show in dialog
        val options = arrayOf("Edit","Delete")

        // alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){_,position->
                // handle item click
                if(position==0){
                    // Edit is clicked
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId",bookId)
                    context.startActivity(intent)
                }else{
                    // Delete is clicked
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }.show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size // item counts
    }

    override fun getFilter(): Filter {
        if (filter==null){
            filter = FilterPdfAdmin(filterList,this)
        }

        return filter as FilterPdfAdmin
    }

    //viewHolder class to hold/init Ui views for row_pdf_admin.xml
    inner class HolderPdfAdmin(itemView: View): RecyclerView.ViewHolder(itemView){
        //init ui views
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
        var moreBtn = binding.moreBtn



    }


}