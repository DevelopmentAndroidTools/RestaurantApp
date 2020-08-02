package com.albino.restaurantapp.activity

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albino.restaurantapp.R
import com.albino.restaurantapp.adapter.CartAdapter
import com.albino.restaurantapp.adapter.RestaurantMenuAdapter
import com.albino.restaurantapp.model.CartItems
import com.albino.restaurantapp.model.RestaurantMenu
import com.albino.restaurantapp.utils.ConnectionManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


lateinit var toolbar:androidx.appcompat.widget.Toolbar
lateinit var textViewOrderingFrom:TextView
lateinit var buttonPlaceOrder: Button
lateinit var recyclerView: RecyclerView
lateinit var layoutManager: RecyclerView.LayoutManager
lateinit var menuAdapter: CartAdapter
lateinit var restaurantId:String
lateinit var restaurantName:String
lateinit var linearLayout:LinearLayout
lateinit var activity_cart_Progressdialog:RelativeLayout
lateinit var selectedItemsId:ArrayList<String>

var totalAmount=0

var cartListItems = arrayListOf<CartItems>()

class CartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        buttonPlaceOrder=findViewById(R.id.buttonPlaceOrder)
        textViewOrderingFrom=findViewById(R.id.textViewOrderingFrom)
        linearLayout=findViewById(R.id.linearLayout)
        toolbar=findViewById(R.id.toolBar)
        activity_cart_Progressdialog=findViewById(R.id.activity_cart_Progressdialog)

        restaurantId=intent.getStringExtra("restaurantId")
        restaurantName=intent.getStringExtra("restaurantName")
        selectedItemsId= intent.getStringArrayListExtra("selectedItemsId")

        //set the restaurant name
        textViewOrderingFrom.text= restaurantName

        buttonPlaceOrder.setOnClickListener(View.OnClickListener {

                val sharedPreferencess=this.getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE)

                if (ConnectionManager().checkConnectivity(this)) {

                    activity_cart_Progressdialog.visibility=View.VISIBLE

                    try {

                        val foodJsonArray=JSONArray()

                        for (foodItem in selectedItemsId){
                            val singleItemObject=JSONObject()
                            singleItemObject.put("food_item_id",foodItem)
                            foodJsonArray.put(singleItemObject)

                        }

                        val sendOrder = JSONObject()

                        sendOrder.put("user_id",sharedPreferencess.getString("user_id","0"))
                        sendOrder.put("restaurant_id",restaurantId.toString())
                        sendOrder.put("total_cost", totalAmount)
                        sendOrder.put("food",foodJsonArray)

                        val queue = Volley.newRequestQueue(this)

                        val url = "http://"+getString(R.string.ip_address)+"/v2/place_order/fetch_result"

                        val jsonObjectRequest = object : JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            sendOrder,
                            Response.Listener {

                                val responseJsonObjectData = it.getJSONObject("data")

                                val success = responseJsonObjectData.getBoolean("success")

                                if (success) {

                                    Toast.makeText(
                                        this,
                                        "Order Placed",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    
                                    createNotification()



                                    val intent= Intent(this,OrderPlacedActivity::class.java)

                                    startActivity(intent)

                                    finishAffinity()//destory all previous activities


                                } else {
                                    val responseMessageServer =
                                        responseJsonObjectData.getString("errorMessage")
                                    Toast.makeText(
                                        this,
                                        responseMessageServer.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                                activity_cart_Progressdialog.visibility=View.INVISIBLE
                            },
                            Response.ErrorListener {

                                println("ssssss"+it)

                                Toast.makeText(
                                    this,
                                    "Some Error occurred!!!",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()

                                headers["Content-type"] = "application/json"
                                headers["token"] = getString(R.string.token)

                                return headers
                            }
                        }
                        jsonObjectRequest.setRetryPolicy( DefaultRetryPolicy(15000,
                        1,
                            1f
                        ))

                        queue.add(jsonObjectRequest)

                    } catch (e: JSONException) {
                        Toast.makeText(
                            this,
                            "Some unexpected error occured!!!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

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
        })


        setToolBar()

        layoutManager = LinearLayoutManager(this)//set the layout manager

        recyclerView = findViewById(R.id.recyclerViewCart)


    }

    fun fetchData(){

        if (ConnectionManager().checkConnectivity(this)) {

            activity_cart_Progressdialog.visibility=View.VISIBLE

            try {

                val queue = Volley.newRequestQueue(this)

                val url = "http://"+getString(R.string.ip_address)+"/v2/restaurants/fetch_result/fetch_with_restaurant_id?restaurantId=" + restaurantId

                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {

                        val responseJsonObjectData = it.getJSONObject("data")

                        val success = responseJsonObjectData.getBoolean("success")

                        if (success) {

                            val data = responseJsonObjectData.getJSONArray("data")

                            //old listener of jsonObjectRequest are still listening therefore clear is used
                            cartListItems.clear()//clear all items to get updated values

                            totalAmount=0

                            for (i in 0 until data.length()) {
                                val cartItemJsonObject = data.getJSONObject(i)

                                if(selectedItemsId.contains(cartItemJsonObject.getString("id")))//if the fetched id is present in the selected id save
                                {

                                    val menuObject = CartItems(
                                        cartItemJsonObject.getString("id"),
                                        cartItemJsonObject.getString("name"),
                                        cartItemJsonObject.getString("cost_for_one"),
                                        cartItemJsonObject.getString("restaurant_id"))

                                    totalAmount= totalAmount+cartItemJsonObject.getString("cost_for_one").toString().toInt()


                                    cartListItems.add(menuObject)

                                }
                                //progressBar.visibility = View.GONE

                                menuAdapter = CartAdapter(
                                    this,//pass the relativelayout which has the button to enable it later
                                    cartListItems
                                )//set the adapter with the data

                                recyclerView.adapter =
                                    menuAdapter//bind the  recyclerView to the adapter

                                recyclerView.layoutManager =
                                    layoutManager //bind the  recyclerView to the layoutManager

                            }

                            //set the total on the button
                            buttonPlaceOrder.text="Place Order(Total:Rs. "+ totalAmount+")"

                        }
                        activity_cart_Progressdialog.visibility=View.INVISIBLE
                    },
                    Response.ErrorListener {

                        Toast.makeText(
                            this,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()

                        activity_cart_Progressdialog.visibility=View.INVISIBLE

                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()

                        headers["Content-type"] = "application/json"
                        headers["token"] = getString(R.string.token)

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

        }
        else {

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
        setSupportActionBar(toolbar)
        supportActionBar?.title="My Cart"
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
            fetchData()//if internet is available fetch data
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
    
    fun createNotification(){
        val notificationId=1;
        val channelId="personal_notification"


            
            val notificationBulider=NotificationCompat.Builder(this,channelId)
            notificationBulider.setSmallIcon(R.drawable.ic_app_icon_log)
            notificationBulider.setContentTitle("Order Placed")
            notificationBulider.setContentText("Your order has been successfully placed!")
            notificationBulider.setStyle(NotificationCompat.BigTextStyle().bigText("Ordered from "+restaurantName+" and amounting to Rs."+ totalAmount))
            notificationBulider.setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManagerCompat=NotificationManagerCompat.from(this)
            notificationManagerCompat.notify(notificationId,notificationBulider.build())

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)//less than oreo
        {
            val name ="Order Placed"
            val description="Your order has been successfully placed!"
            val importance=NotificationManager.IMPORTANCE_DEFAULT
            
            val notificationChannel=NotificationChannel(channelId,name,importance)
            
            notificationChannel.description=description

            val notificationManager=  (getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
            
        }
    }
}

