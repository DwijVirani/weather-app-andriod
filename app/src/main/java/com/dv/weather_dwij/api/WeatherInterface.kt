package com.dv.weather_dwij.api

import com.dv.weather_dwij.models.Weather
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherInterface {
    @GET("/VisualCrossingWebServices/rest/services/timeline/{lat},{lng}")
    suspend fun getWeather(
        @Path("lat") latitude: Double,
        @Path("lng") longitude: Double,
        @Query("key") apiKey: String
    ): Weather
}