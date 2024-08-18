package com.dv.weather_dwij.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.dv.weather_dwij.data.AppDB
import com.dv.weather_dwij.data.Days

class WeatherRepository(application: Application) {
    private var dbInstance: AppDB? = null
    private val weatherDAO = AppDB.getDB(application)?.weatherDAO()

    var allWeatherData: LiveData<List<Days>>? = weatherDAO?.getAllWeatherRecords()

    init {
        this.dbInstance = AppDB.getDB(application)
    }

    fun insertData(data: Days) {
        AppDB.databaseQueryExecutor.execute {
            this.weatherDAO?.insertWeatherData(data)
        }
    }
}