package com.books.brnbooks.books.filter

import android.widget.Filter
import com.books.brnbooks.books.adapter.AdapterPdfUser
import com.books.brnbooks.books.model.ModelPdf

class FilterPdfUser:Filter {

    // array list in which we want to search
    var filterList:ArrayList<ModelPdf>
    // adapter in which filter need to be implemented
    var adapterPdfUser: AdapterPdfUser



    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint
        val results = FilterResults()

        //value should not be null or not empty
        if(constraint != null && constraint.isNotEmpty()) {
            // search value is not empty nor null

            // change to uppercase or lowercase to avoid case senstivity
            constraint = constraint.toString().lowercase()

            val filteredModels = ArrayList<ModelPdf>()
            for (i in  filterList.indices){
                // validate
                if(filterList[i].title.lowercase().contains(constraint)){
                    // add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            // either it is null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence , results: FilterResults) {
        // apply filter cahnges
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf> /* = java.util.ArrayList<com.peelu.booksvilla.ModelPdf> */

        // apply notify changes
        adapterPdfUser.notifyDataSetChanged()
    }
}