package com.example.cityapp.repository

import com.example.cityapp.database.CityDao
import com.example.cityapp.model.City
import kotlinx.coroutines.flow.Flow

class CityRepository(private val cityDao: CityDao) {
    fun getAllCities(): Flow<List<City>> = cityDao.getAllCities()

    suspend fun getCityById(id: Int): City? = cityDao.getCityById(id)

    suspend fun insertCity(city: City) = cityDao.insertCity(city)

    suspend fun updateCity(city: City) = cityDao.updateCity(city)

    suspend fun deleteCity(city: City) = cityDao.deleteCity(city)
}