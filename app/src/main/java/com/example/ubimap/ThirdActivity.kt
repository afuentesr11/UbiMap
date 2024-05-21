package com.example.ubimap

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.ubimap.persistence.retrofit.retrofit2.IOpenWeather
import com.example.ubimap.persistence.retrofit.retrofit2.WeatherAdapter
import com.example.ubimap.persistence.retrofit.data.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.ubimap.persistence.room.AppDatabase

class ThirdActivity : AppCompatActivity() {
    private val TAG = "yaThirdActivity"

    private lateinit var weatherService: IOpenWeather
    private lateinit var weatherAdapter: WeatherAdapter

    lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        // Get coordinates for selected item. Set default ones if  not obtained.
        val timestamp = intent.getLongExtra("timestamp", 0)
        val latitude = intent.getDoubleExtra("latitude", 40.475172)
        val longitude = intent.getDoubleExtra("longitude", -3.461757)

        // Shared prefs. Check if the user identifier is already saved
        val userIdentifier = getUserIdentifier()
        if (userIdentifier == null) {
            Toast.makeText(this, "User ID not set set. Request will not work", Toast.LENGTH_LONG).show()
        }
        Log.d(TAG, "Latitude: $latitude, Longitude: $longitude, Timestamp: $timestamp")

        // Find the TextView and set the coordinates
        val coordinatesTextView: TextView = findViewById(R.id.coordinatesTextView)
        coordinatesTextView.text = "Latitude: $latitude, Longitude: $longitude"

        // Initialize Retrofit to retrieve data from external web service
        initRetrofit()
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewWeather)
        recyclerView.layoutManager = LinearLayoutManager(this)
        weatherAdapter = WeatherAdapter(emptyList())
        recyclerView.adapter = weatherAdapter
        requestWeatherData(latitude, longitude, userIdentifier?: "default_value")

        // Delete item
        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Confirm delete")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { dialog, which ->
                    database = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "coordinates").build()
                    lifecycleScope.launch(Dispatchers.IO) {
                        Log.d(TAG, "Number of items in database before delete "+database.locationDao().getCount()+".");
                        database.locationDao().deleteLocationByTimestamp(timestamp)
                        Log.d(TAG, "Number of items in database after delete "+database.locationDao().getCount()+".");
                        withContext(Dispatchers.Main) {
                            finish() // Cierra la actividad en el hilo principal
                        }
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherService = retrofit.create(IOpenWeather::class.java)
    }

    private fun requestWeatherData(latitude: Double, longitude: Double, apiKey: String) {
        val weatherDataCall = weatherService.getWeatherData(
            latitude = latitude,
            longitude = longitude,
            count = 10,
            apiKey = apiKey
        )

        weatherDataCall.enqueue(object : Callback<WeatherData> {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherResponse ->
                        // Now it's safe to use weatherResponse.list
                        weatherAdapter.updateWeatherData(weatherResponse.list)
                        weatherAdapter.notifyDataSetChanged()
                        Toast.makeText(this@ThirdActivity, "Weather Data Retrieved", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Toast.makeText(this@ThirdActivity, "Response is null", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.e("MainActivity", "Error fetching weather data: ${response.errorBody()?.string()}")
                    Toast.makeText(this@ThirdActivity, "Failed to retrieve data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                // Handle error case
                Log.e("MainActivity", "Failure: ${t.message}")
                Toast.makeText(this@ThirdActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserIdentifier(): String? {
        val sharedPreferences = this.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userIdentifier", null)
    }
}