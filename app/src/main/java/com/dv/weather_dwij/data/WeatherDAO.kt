package com.dv.weather_dwij.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WeatherDAO {
    @Insert
    fun insertWeatherData(weatherData: Days)

    @Query("SELECT * FROM table_weather ORDER BY dateTime DESC")
    fun getAllWeatherRecords() : LiveData<List<Days>>
}