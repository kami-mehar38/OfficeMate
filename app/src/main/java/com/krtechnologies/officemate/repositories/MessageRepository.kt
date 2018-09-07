package com.krtechnologies.officemate.repositories

import android.app.Application
import android.arch.lifecycle.LiveData
import com.krtechnologies.officemate.database.MessageRoomDatabase
import com.krtechnologies.officemate.interfaces.MessageDao
import com.krtechnologies.officemate.models.Message
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import java.util.concurrent.Future


/**
 * Created by ingizly on 9/6/18
 **/
class MessageRepository(application: Application) {

    private var messageDao: MessageDao? = null
    private var allMessages: LiveData<MutableList<Message>>? = null

    init {
        val db = MessageRoomDatabase.getDatabase(application)
        messageDao = db?.messageDao()
        allMessages = messageDao?.getAllMessages()
    }

    fun getAllMessages(): LiveData<MutableList<Message>> {
        return allMessages!!
    }

    fun insert(message: Message): Long? {
        return messageDao?.insert(message)
    }
}