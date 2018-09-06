package com.krtechnologies.officemate.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.graphics.Picture
import android.support.annotation.NonNull
import org.jetbrains.annotations.NotNull
import java.sql.Timestamp

/**
 * Created by ingizly on 9/1/18
 **/

@Entity(tableName = "messages")
data class Message(@NonNull @PrimaryKey val id: Int,
                   @NonNull @ColumnInfo val isMine: Int,
                   @NonNull @ColumnInfo val message: String,
                   @NonNull @ColumnInfo val timestamp: String,
                   @NonNull @ColumnInfo val from: String,
                   @NonNull @ColumnInfo val email: String,
                   @NonNull @ColumnInfo val admin_email: String,
                   @NonNull @ColumnInfo val profilePicture: String) : Comparable<Message> {
    override fun compareTo(other: Message): Int = if (other.id == this.id) 0 else 1

}