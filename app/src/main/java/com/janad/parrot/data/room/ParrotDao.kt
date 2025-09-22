package com.janad.parrot.data.room

import androidx.room.*

@Dao
interface ParrotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCodeNote(note: Product)

    @Update
    suspend fun updateCodeNote(note: Product)

    @Delete
    suspend fun deleteCodeNote(note: Product)

    @Query("SELECT * FROM code_notes ORDER BY createdAt DESC")
    suspend fun getAllNotes(): List<Product>

    @Query("SELECT * FROM code_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Product?
}
