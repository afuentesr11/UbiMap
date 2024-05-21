package com.example.ubimap.persistence.retrofit.data

import com.google.gson.annotations.SerializedName


data class WeatherData (

        @SerializedName("message" ) var message : String?         = null,
        @SerializedName("cod"     ) var cod     : String?         = null,
        @SerializedName("count"   ) var count   : Int?            = null,
        @SerializedName("list"    ) var list    : ArrayList<List> = arrayListOf()

)