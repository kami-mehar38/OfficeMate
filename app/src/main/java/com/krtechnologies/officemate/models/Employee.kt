package com.krtechnologies.officemate.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Employee(
        @SerializedName("id") @Expose val id: String = "",
        @SerializedName("name") @Expose val name: String = "",
        @SerializedName("email") @Expose val email: String = "",
        @SerializedName("profile_picture") @Expose val profilePicture: String = "",
        @SerializedName("password") @Expose val password: String = "",
        @SerializedName("organization") @Expose val organization: String = "",
        @SerializedName("designation") @Expose val designation: String = "",
        @SerializedName("subscription") @Expose val subscription: String = "",
        @SerializedName("isAdmin") @Expose val isAdmin: String = "",
        @SerializedName("joining_date") @Expose val joiningDate: String = "",
        @SerializedName("admin_email") @Expose val adminEmail: String = ""
) {
    override fun toString(): String {
        return "Employee(id='$id', name='$name', email='$email', profilePicture='$profilePicture', password='$password', organization='$organization', designation='$designation', subscription='$subscription', isAdmin='$isAdmin', joiningDate='$joiningDate', adminEmail='$adminEmail')"
    }
}