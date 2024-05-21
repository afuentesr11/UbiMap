package com.example.ubimap.persistence.retrofit.data

import com.google.gson.annotations.SerializedName


data class Coord (

        @SerializedName("lat" ) var lat : Double? = null,
        @SerializedName("lon" ) var lon : Double? = null

)