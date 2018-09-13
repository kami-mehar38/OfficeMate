package com.krtechnologies.officemate.models

import java.io.Serializable

/**
 * Created by ingizly on 9/12/18
 **/
data class Contact(val id: String, val name: String, val phoneNo: String) : Comparable<Contact>, Serializable {
    override fun compareTo(other: Contact): Int = if (other.id == this.id) 0 else 1
    override fun toString(): String {
        return "Contact(id='$id', name='$name', phoneNo='$phoneNo')"
    }
}