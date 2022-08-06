package com.books.brnbooks.books.filter

import android.widget.Filter
import com.books.brnbooks.books.adapter.AdapterCategory
import com.books.brnbooks.books.model.ModelCategory


class FilterCategory: Filter {

    //array list in which we want to search filter
    private var filterList:ArrayList<ModelCategory>

    private var adapterCategory: AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        //value should not be null or not empty
        if(constraint != null && constraint.isNotEmpty()){
            // search value is not empty nor null

            // change to uppercase or lowercase to avoid case senstivity
            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<ModelCategory> = ArrayList()
            for (i in 0 until filterList.size){
                // validate
                if(filterList[i].category.uppercase().contains(constraint)){
                    // add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //apply filter changes
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        //notify changes
        adapterCategory.notifyDataSetChanged()
    }
}