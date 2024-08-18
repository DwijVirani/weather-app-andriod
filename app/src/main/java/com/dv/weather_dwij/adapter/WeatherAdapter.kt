package com.dv.weather_dwij.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dv.weather_dwij.data.Days
import com.dv.weather_dwij.databinding.WeatherItemBinding

class WeatherAdapter(private val context: Context, var reportList: MutableList<Days>) :
    RecyclerView.Adapter<WeatherAdapter.WeatherHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherHolder {
        return WeatherHolder(WeatherItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: WeatherHolder, position: Int) {
        val currentItem: Days = reportList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return reportList.size
    }

    inner class WeatherHolder(b: WeatherItemBinding) : RecyclerView.ViewHolder(b.getRoot()) {
        var binding: WeatherItemBinding

        fun bind(currentItem: Days) {
            if (currentItem != null) {
                binding.tvCity.text = currentItem.city
                binding.tvHumidity.text = "Humidity: ${currentItem.humidity}"
                binding.tvTemp.text = "Temperature: ${currentItem.temp}"
                binding.tvDateTime.text = "Date: ${currentItem.datetime}"
                binding.tvConditions.text = "Conditions: ${currentItem.conditions}"
            }
        }
        init {
            binding = b
        }
    }
}