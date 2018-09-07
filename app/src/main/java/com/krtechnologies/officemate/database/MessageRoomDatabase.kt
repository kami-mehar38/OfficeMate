package com.krtechnologies.officemate.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.krtechnologies.officemate.interfaces.MessageDao
import com.krtechnologies.officemate.models.Message
import android.arch.persistence.room.Room


/**
 * Created by ingizly on 9/6/18
 **/

@Database(entities = [Message::class], version = 1, exportSchema = false)
abstract class MessageRoomDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        private var INSTANCE: MessageRoomDatabase? = null

        @JvmStatic
        @Synchronized
        fun getDatabase(context: Context): MessageRoomDatabase? {
            if (INSTANCE == null) {
                synchronized(MessageRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        // Create database here
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                MessageRoomDatabase::class.java, "messages")
                                .build()
                    }
                }
            }
            return INSTANCE
        }
    }

}