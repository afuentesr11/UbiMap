package com.example.ubimap.persistence.retrofit.data

import com.google.gson.annotations.SerializedName


data class List (

        @SerializedName("id"      ) var id      : Int?               = null,
        @SerializedName("name"    ) var name    : String?            = null,
        @SerializedName("coord"   ) var coord   : Coord?             = Coord(),
        @SerializedName("main"    ) var main    : Main?              = Main(),
        @SerializedName("dt"      ) var dt      : Int?               = null,
        @SerializedName("wind"    ) var wind    : Wind?              = Wind(),
        @SerializedName("sys"     ) var sys     : Sys?               = Sys(),
        @SerializedName("rain"    ) var rain    : String?            = null,
        @SerializedName("snow"    ) var snow    : String?            = null,
        @SerializedName("clouds"  ) var clouds  : Clouds?            = Clouds(),
        @SerializedName("weather" ) var weather : ArrayList<Weather> = arrayListOf()

)
