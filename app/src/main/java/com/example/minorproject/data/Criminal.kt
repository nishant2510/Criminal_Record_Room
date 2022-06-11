package com.example.minorproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender{
    Male,
    Female,
    Other
}

enum class CasePriority{
    Low,
    Medium,
    High
}

@Entity(tableName ="criminal")
data class Criminal(@PrimaryKey(autoGenerate = true) val id:Long,
                    val name: String,
                    val aadhaarNumber: String,
                    val priority: Int,
                    val age: Int,
                    val gender: Int,
                    val crimeDescription: String,
                    val crimeDate: String,
                    val crimeTime: String,
                    val crimeLocation: String,
                    val officerOnDuty: String,
                    val photo: String)

