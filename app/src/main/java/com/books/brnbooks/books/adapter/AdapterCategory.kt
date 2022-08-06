package com.books.brnbooks.books.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.books.brnbooks.books.filter.FilterCategory
import com.books.brnbooks.books.model.ModelCategory
import com.books.brnbooks.PdfListAdminActivity
import com.google.firebase.database.FirebaseDatabase
import com.books.brnbooks.databinding.RowCategoryBinding


class AdapterCategory: RecyclerView.Adapter<AdapterCategory.HolderCategory>,Filterable{

    private var context: Context
    var categoryArrayList: ArrayList<ModelCategory>


    private var filterList:ArrayList<ModelCategory>

    private lateinit var binding: RowCategoryBinding

    private var filter: FilterCategory? = null

    //constructor
    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>):super() {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //inflate/bind the rowCategory.xml
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context),parent,false)

        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        // get data, set data, Handle clicks etc

        //get data
        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
//        var uid = model.uid
//        var timestamp = model.timestamp


        //set data
        holder.categoryTv.text = category

        //handle click, delete category
        holder.deleteBtn.setOnClickListener {
            // confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure, you want to delete this category?")
                .setPositiveButton("Confirm"){_,_ ->
                    Toast.makeText(context,"Deleting...",Toast.LENGTH_SHORT).show()
                    deleteCategory(model,holder)
                }.setNegativeButton("Cancel"){a,_->
                    a.dismiss()
                }.show()
        }

        // handle click, start pdf list admin activity , also pass pdf id, title
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId",id)
            intent.putExtra("category",category)
            context.startActivity(intent)
        }
        }





    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        //get id of category to delete
        val id = model.id

        // Firebase DB > categories > categoryId
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {e->
                Toast.makeText(context,"unable to delete due to ${e.message}",Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }





    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterCategory(filterList,this)
        }
        return filter as FilterCategory
    }

    //viewHolder class to hold/init Ui views for row_category.xml
    inner class HolderCategory(itemView:View):RecyclerView.ViewHolder(itemView){
        //init ui views
        var categoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn
    }


}