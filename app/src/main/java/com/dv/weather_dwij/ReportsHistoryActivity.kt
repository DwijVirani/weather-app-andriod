package com.dv.weather_dwij

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dv.weather_dwij.adapter.WeatherAdapter
import com.dv.weather_dwij.data.Days
import com.dv.weather_dwij.databinding.ActivityReportsHistoryBinding
import com.dv.weather_dwij.repositories.WeatherRepository

class ReportsHistoryActivity : AppCompatActivity() {
    private val TAG = this.javaClass.canonicalName

    private lateinit var binding: ActivityReportsHistoryBinding
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var weatherList: MutableList<Days>
    private lateinit var weatherRepository: WeatherRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReportsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        this.weatherRepository = WeatherRepository(application)

        weatherList = mutableListOf()
        weatherAdapter = WeatherAdapter(applicationContext, weatherList)
        binding!!.rvWeatherItem.setAdapter(weatherAdapter)
        binding!!.rvWeatherItem.layoutManager = LinearLayoutManager(this)
        binding!!.rvWeatherItem.setHasFixedSize(true)
        binding!!.rvWeatherItem.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
    }

    override fun onStart() {
        super.onStart()
        this.weatherRepository.allWeatherData?.observe(this) { receivedData ->
            if (receivedData.isNotEmpty()) {
                weatherList.clear()
                weatherList.addAll(receivedData)
                weatherAdapter.notifyDataSetChanged()
            } else {
                Log.d(TAG, "onStart: No data received from observer")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }
            R.id.reports_history -> {
                startActivity(Intent(this, ReportsHistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}