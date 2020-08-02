package com.albino.restaurantapp.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albino.restaurantapp.R
import com.albino.restaurantapp.adapter.OrderHistoryAdapter
import com.albino.restaurantapp.model.OrderHistoryRestaurant
import com.albino.restaurantapp.utils.ConnectionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

lateinit var layoutManager1: RecyclerView.LayoutManager
lateinit var menuAdapter1: OrderHistoryAdapter
lateinit var recyclerViewAllOrders:RecyclerView
lateinit var toolbarOrderHistroy:androidx.appcompat.widget.Toolbar
lateinit var order_activity_history_Progressdialog:RelativeLayout
lateinit var order_history_fragment_no_orders:RelativeLayout


class OrderHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        recyclerViewAllOrders=findViewById(R.id.recyclerViewAllOrders)

        toolbarOrderHistroy=findViewById(R.id.toolBar)

        order_activity_history_Progressdialog=findViewById(R.id.order_activity_history_Progressdialog)

        order_history_fragment_no_orders=findViewById(R.id.order_history_fragment_no_orders)

        setToolBar()


    }


    fun setItemsForEachRestaurant(){

        layoutManager1=LinearLayoutManager(this)

        val orderedRestaurantList=ArrayList<OrderHistoryRestaurant>()

        val sharedPreferencess=this.getSharedPreferences(getString(R.string.shared_preferences),
            Context.MODE_PRIVATE)

        val user_id=sharedPreferencess.getString("user_id","000")

        if (ConnectionManager().checkConnectivity(this)) {

            order_activity_history_Progressdialog.visibility=View.VISIBLE

            try {

                val queue = Volley.newRequestQueue(this)

                val url = "http://"+getString(R.string.ip_address)+"/v2/orders/fetch_result?userId=" + user_id

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {

                        val responseJsonObjectData = it.getJSONObject("data")

                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {

                            val data = responseJsonObjectData.getJSONArray("data")

                            if(data.length()==0){//no items present display toast

                                Toast.makeText(
                                    this,
                                    "No Orders Placed yet!!!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                order_history_fragment_no_orders.visibility=View.VISIBLE

                            }else
                            {
                                order_history_fragment_no_orders.visibility=View.INVISIBLE



                                for (i in 0 until data.length()) {
                                    val restaurantItemJsonObject = data.getJSONObject(i)

                                    val eachRestaurantObject = OrderHistoryRestaurant(
                                        restaurantItemJsonObject.getString("order_id"),
                                        restaurantItemJsonObject.getString("restaurant_name"),
                                        restaurantItemJsonObject.getString("total_cost"),
                                        restaurantItemJsonObject.getString("order_placed_at").substring(0,10))// only date is extracted

                                    orderedRestaurantList.add(eachRestaurantObject)

                                    menuAdapter1 = OrderHistoryAdapter(
                                        this,
                                        orderedRestaurantList
                                    )//set the adapter with the data

                                    recyclerViewAllOrders.adapter = menuAdapter1//bind the  recyclerView to the adapter

                                    recyclerViewAllOrders.layoutManager = layoutManager1 //bind the  recyclerView to the layoutManager

                                }

                            }

                        }
                        order_activity_history_Progressdialog.visibility=View.INVISIBLE
                    },
                    Response.ErrorListener {
                        order_activity_history_Progressdialog.visibility=View.INVISIBLE

                        Toast.makeText(
                            this,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()

                        headers["Content-type"] = "application/json"
                        headers["token"] = "acdc385cfd7264"

                        return headers
                    }
                }

                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    this,
                    "Some Unexpected error occured!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit"){ text,listener->
                finishAffinity()//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }

    }

    fun setToolBar(){
        setSupportActionBar(toolbarOrderHistroy)
        supportActionBar?.title="My Previous Orders"
        supportActionBar?.setHomeButtonEnabled(true)//enables the button on the tool bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)//displays the icon on the button
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)//change icon to custom
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id=item.itemId

        when(id){
            android.R.id.home->{
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(this)) {
            setItemsForEachRestaurant()//if internet is available fetch data
        }else
        {

            val alterDialog=androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Internet Connection can't be establish!")
            alterDialog.setPositiveButton("Open Settings"){text,listener->
                val settingsIntent= Intent(Settings.ACTION_SETTINGS)//open wifi settings
                startActivity(settingsIntent)
            }

            alterDialog.setNegativeButton("Exit"){ text,listener->
                finishAffinity()//closes all the instances of the app and the app closes completely
            }
            alterDialog.setCancelable(false)

            alterDialog.create()
            alterDialog.show()

        }

        super.onResume()
    }


}
