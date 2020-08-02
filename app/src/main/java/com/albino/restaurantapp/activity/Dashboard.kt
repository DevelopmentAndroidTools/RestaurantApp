package com.albino.restaurantapp.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import com.albino.restaurantapp.R
import com.albino.restaurantapp.adapter.DashboardFragmentAdapter
import com.albino.restaurantapp.fragment.*
import com.albino.restaurantapp.utils.ConnectionManager
import com.google.android.material.navigation.NavigationView

class Dashboard : AppCompatActivity() {

    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var textViewcurrentUser:TextView
    lateinit var textViewMobileNumber:TextView
    lateinit var sharedPreferencess:SharedPreferences



    var previousMenuItemSelected: MenuItem?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
         sharedPreferencess=getSharedPreferences(getString(R.string.shared_preferences),
            Context.MODE_PRIVATE)

        coordinatorLayout=findViewById(R.id.coordinatorLayout)
        toolbar=findViewById(R.id.toolBar)
        frameLayout=findViewById(R.id.frameLayout)
        navigationView=findViewById(R.id.navigationView)
        drawerLayout=findViewById(R.id.drawerLayout)
        val headerView=navigationView.getHeaderView(0)
        textViewcurrentUser=headerView.findViewById(R.id.textViewcurrentUser)
        textViewMobileNumber=headerView.findViewById(R.id.textViewMobileNumber)

        //is used to set the default dashboard to checked when the app opens or the back btn is pressed to go to the previous fragment
        navigationView.menu.getItem(0).setCheckable(true)
        navigationView.menu.getItem(0).setChecked(true)


        //set tool bar
        setToolBar()


        //set user details
        textViewcurrentUser.text=sharedPreferencess.getString("name","Albino Braganza")
        textViewMobileNumber.text="+91-"+sharedPreferencess.getString("mobile_number","9999999999")


        val actionBarDrawerToggle=  ActionBarDrawerToggle(
            this@Dashboard,
            drawerLayout,
            R.string.open_drawer,//hamburger icon to open
            R.string.close_drawer
        )//once opened it changes to back arrow

        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        actionBarDrawerToggle.syncState()//to sync with the state of the navigation toggle with the state of the navigation drawer

        navigationView.setNavigationItemSelectedListener {

            if(previousMenuItemSelected!=null){
                previousMenuItemSelected?.setChecked(false)
            }

            previousMenuItemSelected=it//the current fragment will be previous fragment when a user clicks a new fragment

            it.setCheckable(true)//if not the first time set the current to checked
            it.setChecked(true)


            when(it.itemId){//it holds the id of the currently selected item
                R.id.homee ->{
                    openDashboard()//function called below as the same code is present in the oncreate of the activity
                    drawerLayout.closeDrawers()
                }
                R.id.myProfile ->{
                    //the supportFragmentManager is responsible for starting a new fragment and each call is called a transaction
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frameLayout,
                            ProfileFragment(this)
                        )//replace the old layout with the new frag  layout
                        //.addToBackStack("Favourite")//so that the app foesnt close on clicking the backbtn, it store the fragments in backstack and goies to previous fragment
                        .commit()//apply changes

                    supportActionBar?.title="My Profile"//change the title when each new fragment is opened

                    drawerLayout.closeDrawers()

                    Toast.makeText(this@Dashboard,"My Profile", Toast.LENGTH_SHORT).show()
                }
                R.id.favouriteRestaurants ->{

                    //the supportFragmentManager is responsible for starting a new fragment and each call is called a transaction
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frameLayout,
                            FavouriteRestaurantFragment(this)
                        )//replace the old layout with the new frag  layout
                        //.addToBackStack("Favourite")//so that the app doesnt close on clicking the backbtn, it store the fragments in backstack and goies to previous fragment
                        .commit()//apply changes

                    supportActionBar?.title="Favourite Restaurants"//change the title when each new fragment is opened

                    drawerLayout.closeDrawers()

                }
                R.id.orderHistory ->{
                    val intent= Intent(this, OrderHistoryActivity::class.java)

                    drawerLayout.closeDrawers()

                    startActivity(intent)

                }
                R.id.faqs ->{

                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frameLayout,
                            FaqsFragment()
                        )//replace the old layout with the new frag  layout
                        //.addToBackStack("Favourite")//so that the app foesnt close on clicking the backbtn, it store the fragments in backstack and goies to previous fragment
                        .commit()//apply changes

                    supportActionBar?.title="Frequently Asked Questions"//change the title when each new fragment is opened

                    drawerLayout.closeDrawers()

                    Toast.makeText(this@Dashboard,"FAQs", Toast.LENGTH_SHORT).show()
                }
                R.id.logout ->{

                    drawerLayout.closeDrawers()

                    val alterDialog=androidx.appcompat.app.AlertDialog.Builder(this)

                    alterDialog.setTitle("Confirmation")
                    alterDialog.setMessage("Are you sure you want to log out?")
                    alterDialog.setPositiveButton("Yes"){text,listener->
                        sharedPreferencess.edit().putBoolean("user_logged_in",false).apply()

                        ActivityCompat.finishAffinity(this)//closes all the instances of the app and the app closes completely
                    }

                    alterDialog.setNegativeButton("No"){ text,listener->

                    }
                    alterDialog.create()
                    alterDialog.show()

                }
            }

            return@setNavigationItemSelectedListener true
        }

        openDashboard()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id=item.itemId

        if(id==android.R.id.home){//home id is the id of the button on the tool bar
            drawerLayout.openDrawer(GravityCompat.START)//start he drawer from the start
        }

        return super.onOptionsItemSelected(item)
    }



    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)//gets the id og=f the current fragment

        when(currentFragment){
            !is DashboardFragment -> {
                navigationView.menu.getItem(0).setChecked(true)
                openDashboard()}
            else ->super.onBackPressed()
        }
    }

    fun setToolBar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title="All Restaurants"
        supportActionBar?.setHomeButtonEnabled(true)//enables the button on the tool bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)//displays the icon on the button
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)//change icon to custom
    }


    fun openDashboard(){
        val transaction=supportFragmentManager.beginTransaction()

        transaction.replace(
            R.id.frameLayout,
            DashboardFragment(this)
        )//replace the old layout with the new frag  layout

        transaction .commit()//apply changes

        supportActionBar?.title="All Restaurants"//change the title when each new fragment is opened



        navigationView.setCheckedItem(R.id.homee)
    }

    //to disable auto pop of soft keyboard on the search
    override fun onResume() {
        getWindow().setSoftInputMode(WindowManager.
            LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume()
    }




}
