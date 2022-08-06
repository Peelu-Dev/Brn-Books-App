package com.books.brnbooks.books.main



import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.books.brnbooks.books.model.ModelCategory
import com.books.brnbooks.books.screens.DonateActivity
import com.books.brnbooks.books.screens.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.books.brnbooks.databinding.ActivityDashboardUserBinding

class DashboardUserActivity : AppCompatActivity() {
    //viewBinding
    private lateinit var binding: ActivityDashboardUserBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList:ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter:ViewPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.nibBack.setOnClickListener {
            onBackPressed()
        }

        // handle click,logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager){
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )
        // init list
        categoryArrayList = ArrayList()

        // load categories from db
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // clear array list
                categoryArrayList.clear()

                /* load some static categories such as Most Viewed, Most Downloaded*./
                // Add data to models
                 */
                val modelAll = ModelCategory("01","All", 1,"")
                val modelMostViewed = ModelCategory("01","Most Viewed",1,"")
                val modelMostDownloaded = ModelCategory("01","Most Downloaded",1,"")

                // add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)

                // add to view Pager Adapter
                viewPagerAdapter.addFragment(
                    BookUserFragment.newInstance(
//                        "${modelAll.id}",
                        "" + modelAll.id,
                        ""+ modelAll.category,
                        "" + modelAll.uid,
                    ),modelAll.category
                )

//                viewPagerAdapter.addFragment(
//                    BookUserFragment.newInstance(
//                        ""+modelMostViewed.id,
//                        ""+modelMostViewed.category,
//                        modelMostViewed.uid
//                    ),modelMostViewed.category
//                )
//                viewPagerAdapter.addFragment(
//                    BookUserFragment.newInstance(
//                        ""+modelMostDownloaded.id,
//                        ""+modelMostDownloaded.category,
//                        ""+modelMostDownloaded.uid
//                    ),modelMostDownloaded.category
//                )

                // refresh list
                viewPagerAdapter.notifyDataSetChanged()

                // now Load from firebase db
                for (ds in snapshot.children){
                    // get data in model
                    val model = ds.getValue(ModelCategory::class.java)

                    // add to list
                    categoryArrayList.add(model!!)

                    // add to view Pager Adapter
                    viewPagerAdapter.addFragment(
                        BookUserFragment.newInstance(
                            ""+model.id,
                            ""+model.category,
                            model.uid
                        ),model.category
                    )
                    // refresh list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        // setup adapter to view Pager
        viewPager.adapter = viewPagerAdapter
//        viewPager.adapter =PagerAdapter?
    }

    class ViewPagerAdapter(fm:FragmentManager, behavior:Int, context: Context): FragmentPagerAdapter(fm,behavior) {

        // holds list of fragments i.e new instances of same fragments for each category
        private val fragmentList:ArrayList<BookUserFragment> = ArrayList()

        // list of titles for categories, for tabs
        private val fragmentTitleList:ArrayList<String> = ArrayList()

        private val context:Context

        init {
            this.context = context
        }
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

         fun addFragment(fragment:BookUserFragment,title:String){
            // add fragment that will passed as parameter in fragment list
            fragmentList.add(fragment)
            // add title that will passed as parameter
            fragmentTitleList.add(title)
        }

    }


    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser==null){
            // not logged in , user can stay in user dashboard without login too
            val setText = "Not Logged In"
            binding.subtitleTv.text = setText
        }else{
            //  logged in , get and show user info
            val email = firebaseUser.email
            // set to textview to toolbar
            binding.subtitleTv.text = email
        }
    }
}