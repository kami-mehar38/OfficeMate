package com.krtechnologies.officemate.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.krtechnologies.officemate.repositories.MessageRepository
import android.arch.lifecycle.LiveData


/**
 * Created by ingizly on 9/6/18
 **/
class MessageViewModel(application: Application) : AndroidViewModel(application) {

    private var mRepository: MessageRepository? = null
    private var allMessages: LiveData<MutableList<Message>>? = null

    init {
        mRepository = MessageRepository(application)
        allMessages = mRepository?.getAllMessages()
    }

    fun getAllMessages(): LiveData<MutableList<Message>> = allMessages!!

    fun insert(message: Message): Long? {
        return mRepository?.insert(message)
    }

}