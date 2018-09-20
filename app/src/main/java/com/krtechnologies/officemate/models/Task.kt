package com.krtechnologies.officemate.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Task(
        @SerializedName("id") @Expose val id: String = "",
        @SerializedName("title") @Expose val title: String = "",
        @SerializedName("description") @Expose val description: String = "",
        @SerializedName("email") @Expose val email: String = "",
        @SerializedName("admin_email") @Expose val adminEmail: String = "",
        @SerializedName("timestamp") @Expose val timestamp: String = "",
        @SerializedName("isDone") @Expose val isDone: String = ""
) : Serializable, Comparable<Task> {
    override fun compareTo(other: Task): Int = if (this.id == other.id) 0 else 1
    override fun toString(): String {
        return "Task(id='$id', title='$title', description='$description', email='$email', adminEmail='$adminEmail', timestamp='$timestamp', isDone='$isDone')"
    }
}