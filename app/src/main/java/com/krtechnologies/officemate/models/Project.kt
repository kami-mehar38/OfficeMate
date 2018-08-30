package com.krtechnologies.officemate.models

import com.google.gson.annotations.SerializedName

data class Project(
        @SerializedName("id") val id: String = "",
        @SerializedName("project_name") val projectName: String = "",
        @SerializedName("project_description") val projectDescription: String = "",
        @SerializedName("eta") val eta: String = "",
        @SerializedName("completion") val completion: String = "",
        @SerializedName("assigned_to") val assignedTo: String = "",
        @SerializedName("email") val email: String = "",
        @SerializedName("admin_email") val adminEmail: String = "",
        @SerializedName("timestamp") val timestamp: String = "",
        @SerializedName("profile_picture") val profilePicture: String = ""
) : Comparable<Project> {

    override fun compareTo(other: Project): Int = if (other.id == this.id) 0 else 1
}