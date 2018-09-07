package com.krtechnologies.officemate.interfaces

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.krtechnologies.officemate.models.Message

/**
 * Created by ingizly on 9/6/18
 **/

@Dao
interface MessageDao {
    @Insert
    fun insert(message: Message): Long

    @Query("DELETE FROM messages")
    fun deleteAll();

    @Query("SELECT * from messages ORDER BY id ASC")
    fun getAllMessages(): LiveData<MutableList<Message>>
}
