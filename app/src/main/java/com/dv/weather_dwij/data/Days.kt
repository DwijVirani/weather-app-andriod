package com.dv.weather_dwij.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_weather")
class Days(
    val city: String?,
    val datetime: String,
    val temp: Double,
    val humidity: Double,
    val conditions: String,
    val description: String,
    val time: String?
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    override fun toString(): String {
        return "Days(id: $id, dateTime=$datetime, temp: $temp, humidity: $humidity, conditions: $conditions)"
    }
}