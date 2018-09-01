package com.krtechnologies.officemate.models

/**
 * Created by ingizly on 9/1/18
 **/


data class Message(val id: Int, val isMine: Int, val message: String) : Comparable<Message> {
    override fun compareTo(other: Message): Int = if (other.id == this.id) 0 else 1

}