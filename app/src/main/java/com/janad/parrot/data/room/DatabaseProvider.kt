package com.janad.notepp.data.room

import android.content.Context
import androidx.room.Room
import com.janad.parrot.data.room.ParrotDatabase

class DatabaseProvider private constructor(context: Context) {
    val db: ParrotDatabase = Room.databaseBuilder(
        context.applicationContext,
        ParrotDatabase::class.java,
        "parrot_db"
    ).build()

    companion object {
        @Volatile
        private var INSTANCE: DatabaseProvider? = null

        fun getInstance(context: Context): DatabaseProvider {
            return INSTANCE ?: synchronized(this) {
                DatabaseProvider(context).also { INSTANCE = it }
            }
        }
    }
}
