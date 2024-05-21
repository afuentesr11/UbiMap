package com.example.ubimap.persistence.retrofit.retrofit2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ubimap.R

class WeatherAdapter(private var weatherList: List<com.example.ubimap.persistence.retrofit.data.List>): RecyclerView.Adapter<WeatherViewHolder>() {

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weather, parent, false)
        return WeatherViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the contents of the view with that element
        val weatherItem = weatherList[position]
        holder.cityName.text = weatherItem.name
        holder.temperature.text = "\uD83C\uDF21\uFE0F Temperature: ${kelvinToCelsius(weatherItem.main?.temp ?: 0.0)}ºC "
        holder.tempFL.text = "Feels like: ${tempKelvin2Icon(weatherItem.main?.feelsLike ?:0.0)}"
        holder.tempMax.text = "Max: ${kelvinToCelsius(weatherItem.main?.tempMax ?: 0.0)}ºC"
        holder.tempMin.text = "Min: ${kelvinToCelsius(weatherItem.main?.tempMin ?: 0.0)}ºC"
        holder.humidity.text = "Humidity: ${weatherItem.main?.humidity ?: 0.0}%"
        holder.windSpeed.text = "\uD83C\uDF2A\uFE0F ${mphToKmh(weatherItem.wind?.speed ?: 0.0)} Kmh"
        holder.weatherM.text =  "${weatherItem.weather[0]?.main?: "ND"} / ${weatherItem.weather[0]?.description?: "ND"}"
        Glide.with(holder.itemView.context).load("http://openweathermap.org/img/wn/${weatherItem.weather[0]?.icon?: "04d.png"}.png").into(holder.icon)
        //Glide.with(holder.itemView.context).load("http://openweathermap.org/img/wn/04d.png").into(holder.icon)
        // e.g. http://openweathermap.org/img/wn/04d.png
    }

    fun kelvinToCelsius(kelvin: Double?): String {
        return String.format("%.1f", kelvin?.minus(273.15))
    }

    fun mphToKmh(mph: Double): String {
        val kmh = mph * 1.60934
        return String.format("%.2f", kmh)
    }

    fun tempKelvin2Icon(tempKelvin: Double): String {
        val tempCelsius = tempKelvin - 273.15
        return when {
            tempCelsius <= 10 -> "🥶" // Cold
            tempCelsius in 11.0..25.0 -> "😀" // Nice
            else -> "🥵" // Hot
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = weatherList.size

    // Method to update weather data and notify adapter
    fun updateWeatherData(newWeatherList: List<com.example.ubimap.persistence.retrofit.data.List>) {
        weatherList = newWeatherList
        notifyDataSetChanged() // Notify any registered observers that the data set has changed.
    }
}