package com.example.ubimap

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ubimap.persistence.room.AppDatabase

class SecondActivity : AppCompatActivity() {
    private val TAG = "yaSecondActivity"

    private lateinit var listView: ListView
    private lateinit var adapter: CoordinatesAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        Log.d(TAG, "onCreate: The activity is being created.")

        // ButtomNavigationMenu
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { item ->
            val currentActivity = this::class.java.simpleName
            when (item.itemId) {
                R.id.navigation_home -> if (currentActivity != MainActivity::class.java.simpleName) {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                R.id.navigation_map -> if (currentActivity != OpenStreetMapActivity::class.java.simpleName) {
                    startActivity(Intent(this, OpenStreetMapActivity::class.java))
                }
                R.id.navigation_list -> if (currentActivity != SecondActivity::class.java.simpleName) {
                    startActivity(Intent(this, SecondActivity::class.java))
                }
            }
            true
        }

        // Inflate heading and add to ListView
        listView = findViewById(R.id.lvCoordinates)
        val headerView = layoutInflater.inflate(R.layout.listview_header, listView, false)
        listView.addHeaderView(headerView, null, false)

        // Init adapter
        adapter = CoordinatesAdapter(this, mutableListOf())
        listView.adapter = adapter

        // Init database
        database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "coordinates").build()

    }

    override fun onResume() {
        super.onResume()

        // Reutiliza el adaptador si ya está inicializado, en lugar de crear uno nuevo
        if (!::adapter.isInitialized) {
            adapter = CoordinatesAdapter(this, mutableListOf())
            listView.adapter = adapter
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val itemCount = database.locationDao().getCount()
            Log.d(TAG, "Number of items in database $itemCount.")
            loadCoordinatesFromDatabase(adapter)
        }

    }


    private class CoordinatesAdapter(context: Context, private val coordinatesList: MutableList<List<String>>) :
        ArrayAdapter<List<String>>(context, R.layout.listview_item, coordinatesList) {

        private val inflater: LayoutInflater = LayoutInflater.from(context)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: inflater.inflate(R.layout.listview_item, parent, false)

            val timestampTextView: TextView = view.findViewById(R.id.tvTimestamp)
            val latitudeTextView: TextView = view.findViewById(R.id.tvLatitude)
            val longitudeTextView: TextView = view.findViewById(R.id.tvLongitude)

            try{
                val item = coordinatesList[position]
                timestampTextView.text = formatTimestamp(item[0].toLong())
                latitudeTextView.text = formatCoordinate(item[1].toDouble())
                longitudeTextView.text = formatCoordinate(item[2].toDouble())

                // move to next activity
                view.setOnClickListener {
                    val intent = Intent(context, ThirdActivity::class.java).apply {
                        putExtra("timestamp", item[0].toLong())
                        putExtra("latitude", item[1].toDouble())
                        putExtra("longitude", item[2].toDouble())
                    }
                    context.startActivity(intent)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CoordinatesAdapter", "getView: Exception parsing coordinates.")
            }
            return view
        }

        private fun formatTimestamp(timestamp: Long): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(timestamp))
        }

        private fun formatCoordinate(value: Double): String {
            return String.format("%.6f", value)
        }

        fun updateData(newData: MutableList<List<String>>) {
            this.coordinatesList.clear()
            this.coordinatesList.addAll(newData)
            notifyDataSetChanged()
        }

    }


    private fun loadCoordinatesFromDatabase(adapter: CoordinatesAdapter) {
        lifecycleScope.launch(Dispatchers.IO) {
            val coordinatesList = database.locationDao().getAllLocations()
            val formattedList = coordinatesList.map { listOf(it.timestamp.toString(), it.latitude.toString(), it.longitude.toString()) }.toMutableList()
            withContext(Dispatchers.Main) {
                adapter.updateData(formattedList)
            }
            Log.d("CoordinatesAdapter", "Number of items in database "+database.locationDao().getCount()+".")
        }
    }

}