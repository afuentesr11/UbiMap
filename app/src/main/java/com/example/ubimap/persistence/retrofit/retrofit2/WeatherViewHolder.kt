package com.example.ubimap.persistence.retrofit.retrofit2

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ubimap.R

class WeatherViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val cityName: TextView = view.findViewById(R.id.tvCityName)
    val temperature: TextView = view.findViewById(R.id.tvTemperature)
    val tempFL: TextView = view.findViewById(R.id.tvTemperatureFeelsLike)
    val tempMax: TextView = view.findViewById(R.id.tvTemperatureMax)
    val tempMin: TextView = view.findViewById(R.id.tvTemperatureMin)
    val humidity: TextView = view.findViewById(R.id.tvHumidity)
    val windSpeed: TextView = view.findViewById(R.id.tvWindSpeed)
    val weatherM: TextView = view.findViewById(R.id.tvWeatherMain)
    val icon: ImageView = view.findViewById(R.id.ivWeatherIcon)
}