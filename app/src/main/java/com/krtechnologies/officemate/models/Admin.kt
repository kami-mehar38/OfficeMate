package com.krtechnologies.officemate.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Admin(
        @SerializedName("name") @Expose val name: String,
        @SerializedName("profile_picture") @Expose val profilePicture: String,
        @SerializedName("email") @Expose val email: String,
        @SerializedName("password") @Expose val password: String,
        @SerializedName("organization") @Expose val organization: String,
        @SerializedName("designation") @Expose val designation: String,
        @SerializedName("joining_date") @Expose val joiningDate: String,
        @SerializedName("subscription") @Expose val subscription: String,
        @SerializedName("token") @Expose val token: String


) {
    override fun toString(): String {
        return "Admin(name='$name', profilePicture='$profilePicture', email='$email', password='$password', organization='$organization', designation='$designation', joiningDate='$joiningDate', subscription='$subscription', token='$token')"
    }
}