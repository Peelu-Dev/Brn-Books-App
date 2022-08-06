package com.books.brnbooks.books.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.books.brnbooks.books.activity.PdfDetailActivity
import com.books.brnbooks.books.filter.FilterPdfUser
import com.books.brnbooks.books.main.MyApplication
import com.books.brnbooks.books.model.ModelPdf
import com.books.brnbooks.databinding.RowPdfUserBinding

class AdapterPdfUser:RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>,Filterable {

    //context
    private var context: Context
    // array list to hold pdfs
     var pdfArrayList:ArrayList<ModelPdf>

    // arrayList to hold filtered pdf
    private var filterList:ArrayList<ModelPdf>


    // filter object
    private var filter: FilterPdfUser? = null

  private lateinit var binding:RowPdfUserBinding

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }





    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        //inflate/bind the row_pdf_user.xml
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPdfUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        // get data, set data, Handle clicks etc

        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val  title = model.title
        val description = model.description
        val  uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        // convert timestamp to dd/MM/yyyy format
        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        // set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate
        holder.categoryTv.text = categoryId

        // we don't need page number here , pass null for page number || load pdf thumbnail
        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)


        // load further details like category, pdf from url, pdf size
//        MyApplication.loadCategory(categoryId,holder.categoryTv)

        //load pdf size
        MyApplication.loadPdfSize(url, title, holder.sizeTv)

        // handle item click, open PdfDetailActivity
        holder.itemView.setOnClickListener {
            // intent with book id
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId",bookId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size // item counts/ number of records
    }


    override fun getFilter(): Filter {
        if (filter==null){
            filter = FilterPdfUser(filterList,this)

        }
        return filter as FilterPdfUser
    }

    //viewHolder class to hold/init Ui views for row_pdf_user.xml
    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder(itemView){
        //init ui views
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv
    }


}