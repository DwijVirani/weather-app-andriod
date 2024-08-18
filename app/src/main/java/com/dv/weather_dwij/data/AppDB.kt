package com.dv.weather_dwij.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.Executors

@Database(entities = [Days::class], version = 4, exportSchema = false)
abstract class AppDB: RoomDatabase() {
    abstract fun weatherDAO(): WeatherDAO

    companion object{
        private var dbInstance: AppDB? = null
        private const val NUMBER_OF_THREADS = 4
        val databaseQueryExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS)

        fun getDB(context: Context): AppDB? {
            if(dbInstance == null) {
                dbInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    name = "com.dv.weather_dwij"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return dbInstance
        }
    }
}