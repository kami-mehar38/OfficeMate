package com.krtechnologies.officemate.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Employee(
        @SerializedName("id") @Expose val id: Int,
        @SerializedName("name") @Expose val name: String,
        @SerializedName("profile_picture") @Expose val profilePicture: String,
        @SerializedName("email") @Expose val email: String,
        @SerializedName("password") @Expose val password: String,
        @SerializedName("organization") @Expose val organization: String,
        @SerializedName("designation") @Expose val designation: String,
        @SerializedName("joining_date") @Expose val joiningDate: String,
        @SerializedName("admin_email") @Expose val adminEmail: String,
        @SerializedName("isAdmin") @Expose val isAdmin: String
) {
    override fun toString(): String {
        return "Employee(id=$id, name='$name', profilePicture='$profilePicture', email='$email', password='$password', organization='$organization', designation='$designation', joiningDate='$joiningDate', adminEmail='$adminEmail', isAdmin='$isAdmin')"
    }
}