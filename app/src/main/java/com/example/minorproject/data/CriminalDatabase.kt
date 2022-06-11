package com.example.minorproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Criminal::class], version = 3)
abstract class CriminalDatabase : RoomDatabase() {

    abstract fun criminalListDao(): CriminalListDao
    abstract fun criminalDetailDao(): CriminalDetailDao

    companion object {
        @Volatile
        private var instance: CriminalDatabase? = null
        fun getDatabase(context: Context) = instance?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                CriminalDatabase::class.java,
                "criminal_database"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}