package com.example.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CodeDao {
    @Query("Select * from code order by cost, code")
    fun getAllCodes(): LiveData<List<Code>>

    @Query("Select * from code where id = :id")
    fun getCode(id: Int): Code

    @Query("Delete from code")
    fun deleteAllCodes()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCode(code: Code)

    @Insert
    fun insertAllCodes(vararg codes: Code)

    @Delete
    fun deleteCode(code: Code)
}