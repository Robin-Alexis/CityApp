package com.example.cityapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cityapp.model.City

@Database(entities = [City::class], version = 1, exportSchema = false)
abstract class CityDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao

    companion object {
        @Volatile
        private var Instance: CityDatabase? = null

        fun getDatabase(context: Context): CityDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CityDatabase::class.java, "city_database")
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
