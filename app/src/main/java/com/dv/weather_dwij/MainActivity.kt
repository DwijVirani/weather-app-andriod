package com.dv.weather_dwij

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.dv.weather_dwij.api.RetrofitInstance
import com.dv.weather_dwij.api.WeatherInterface
import com.dv.weather_dwij.data.Days
import com.dv.weather_dwij.databinding.ActivityMainBinding
import com.dv.weather_dwij.models.Weather
import com.dv.weather_dwij.repositories.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity(), OnClickListener {
    private val TAG = this.javaClass.canonicalName

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var globalWeatherData: Days = Days("","", 0.0, 0.0, "", "", "")
    private lateinit var weatherRepository: WeatherRepository

    private val APP_PERMISSIONS_LIST = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val multiplePermissionsResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) {
        resultsList ->
        var allPermissionsGrantedTracker = true

        for (item in resultsList.entries) {
            if (item.key in APP_PERMISSIONS_LIST && item.value == false) {
                allPermissionsGrantedTracker = false
            }
        }

        if (allPermissionsGrantedTracker == true) {
            Snackbar.make(binding.root, "All permissions granted", Snackbar.LENGTH_LONG).show()
            getDeviceLocation()

        } else {
            Snackbar.make(binding.root, "Some permissions NOT granted", Snackbar.LENGTH_LONG).show()
            handlePermissionDenied()
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location === null) {
                    Log.d(TAG, "Location is null")
                    return@addOnSuccessListener
                }
                val message = "The device is located at: ${location.latitude}, ${location.longitude}"
                Log.d(TAG, message)
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            }
    }

    private fun handlePermissionDenied() {
        binding.tvError.text = "Sorry, you need to give us permissions before we can get your location. Check your settings menu and update your location permissions for this app."
        binding.btnSearch.isEnabled = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(this.binding.menuToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        this.weatherRepository = WeatherRepository(application)
        multiplePermissionsResultLauncher.launch(APP_PERMISSIONS_LIST)
        this.getCurrentLocation()

        binding.btnSearch.setOnClickListener(this)
        binding.btnSaveReport.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btnSearch -> onSearchClick()
            R.id.btnSaveReport -> saveReport()
        }
    }

    private fun onSearchClick() {
        val geocoder:Geocoder = Geocoder(applicationContext, Locale.getDefault())
        val cityFromUI = binding.etCity.text.toString()

        Log.d(TAG, "Getting coordinates for $cityFromUI")

        try {
            if(cityFromUI.isNullOrEmpty()) {
                binding.tvError.text = "Please enter a city name to get weather report"
                binding.etCity.error = "City is required"
            } else {
                val searchResults:MutableList<Address>? = geocoder.getFromLocationName(cityFromUI, 1)
                if (searchResults == null) {
                    Log.e(TAG, "searchResults variable is null")
                    return@onSearchClick
                }

                if (searchResults.size == 0) {
                    binding.tvError.text = "Search results are empty."
                } else {
                    binding.tvError.text = ""
                    lifecycleScope.launch {
                        var api: WeatherInterface = RetrofitInstance.retrofitService
                        val apiKey = "2C3KNKGQPTNA5487JHB2KTJU7"

                        val foundLocation:Address = searchResults[0]
                        val lat = foundLocation.latitude
                        val lng = foundLocation.longitude

                        val weatherData = api.getWeather(lat, lng, apiKey)
                        val currentData = weatherData.days[0]

                        var tempInF: Double = currentData.temp
                        val tempInC = ((tempInF - 32) * 5/9)
                        val roundedTempInC = String.format("%.2f", tempInC).toDouble()

                        globalWeatherData = Days(cityFromUI, currentData.datetime, roundedTempInC, currentData.humidity, currentData.conditions, currentData.description, weatherData.currentConditions.datetime)

                        binding.tvCity.text = cityFromUI
                        binding.tvTemp.text = "Temperature: $roundedTempInC C"
                        binding.tvHumidity.text = "Humidity: ${currentData.humidity}"
                        binding.tvConditions.text = "Conditions: ${currentData.conditions}"
                        binding.tvDateTime.text = "Report Date: ${currentData.datetime} ${weatherData.currentConditions.datetime}"
                        binding.tvDescription.text = "Description ${currentData.description}"
                    }
                }
            }

        } catch(ex:Exception) {
            Log.e(TAG, "Error encountered while getting coordinate location.")
            Log.e(TAG, ex.toString())
        }
    }

    private fun saveReport() {
        if(globalWeatherData.city.isNullOrEmpty()) {
            binding.tvError.text = "No report available to save. Please generate a report first"
        } else {
            lifecycleScope.launch {
                weatherRepository.insertData(globalWeatherData)
                Snackbar.make(binding.root, "Report Saved", Snackbar.LENGTH_SHORT).show()

                binding.tvCity.text = ""
                binding.tvTemp.text = ""
                binding.tvHumidity.text = ""
                binding.tvConditions.text = ""
                binding.tvDateTime.text = ""
                binding.tvDescription.text = ""
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

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                val apiKey = "2C3KNKGQPTNA5487JHB2KTJU7"
                var city:String = ""
                val geocoder: Geocoder = Geocoder(applicationContext, Locale.getDefault())
                if (location === null) {
                    Log.d(TAG, "Location is null")
                    return@addOnSuccessListener
                }
                val message = "The device is located at: ${location.latitude}, ${location.longitude}"
                try {
                    val searchResults: MutableList<Address>? =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (searchResults == null) {
                        Log.d(TAG, "ERROR: When retrieving results")
                    }
                    else if (searchResults.size == 0){
                        Log.d(TAG, "ERROR: No result found")
                    }
                    else{
                        city = searchResults[0].locality
                        val cityEditText = findViewById<EditText>(R.id.etCity)
                        cityEditText.setText(city)                    }
                }catch (exception:Exception) {
                    Log.d(TAG, "Exception occurred while getting matching address")
                    Log.d(TAG, exception.toString())
                }

                var api:WeatherInterface = RetrofitInstance.retrofitService
                lifecycleScope.launch {
                    val weatherData: Weather = api.getWeather(location.latitude, location.longitude, apiKey)
                    val currentData = weatherData.days[0]

                    var tempInF: Double = currentData.temp
                    val tempInC = ((tempInF - 32) * 5/9)
                    val roundedTempInC = String.format("%.2f", tempInC).toDouble()

                    globalWeatherData = Days(city, currentData.datetime, roundedTempInC, currentData.humidity, currentData.conditions, currentData.description, weatherData.currentConditions.datetime)

                    binding.tvCity.text = city
                    binding.tvTemp.text = "Temperature: $roundedTempInC C"
                    binding.tvHumidity.text = "Humidity: ${currentData.humidity}"
                    binding.tvConditions.text = "Conditions: ${currentData.conditions}"
                    binding.tvDateTime.text = "Report Date: ${currentData.datetime} ${weatherData.currentConditions.datetime}"
                    binding.tvDescription.text = "Description ${currentData.description}"
                }
                Log.d(TAG, message)
            }
    }

    override fun onResume() {
        super.onResume()
        this.getCurrentLocation()
    }
}