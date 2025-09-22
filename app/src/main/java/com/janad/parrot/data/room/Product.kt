package com.janad.parrot.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "code_notes")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val mediaIds: List<Int>,
    val finished: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
