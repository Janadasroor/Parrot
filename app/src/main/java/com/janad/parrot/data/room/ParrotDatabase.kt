package com.janad.parrot.data.room
import android.content.Context
import androidx.room.Database
import androidx.room.Room

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(
    entities = [Product::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ParrotDatabase : RoomDatabase() {
    abstract fun parrotDao(): ParrotDao

    companion object {
        @Volatile
        private var INSTANCE: ParrotDatabase? = null

        fun getInstance(context: Context): ParrotDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ParrotDatabase::class.java,
                    "parrot_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}


class Converters {

    @TypeConverter
    fun fromIntList(list: List<Int>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIntList(data: String): List<Int> {
        return if (data.isEmpty()) emptyList()
        else data.split(",").map { it.toInt() }
    }
}