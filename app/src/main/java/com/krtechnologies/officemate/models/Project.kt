package com.krtechnologies.officemate.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Project(
        @SerializedName("id") val id: String = "",
        @SerializedName("project_name") val projectName: String = "",
        @SerializedName("project_description") var projectDescription: String = "",
        @SerializedName("eta") var eta: String = "",
        @SerializedName("completion") var completion: String = "",
        @SerializedName("assigned_to") val assignedTo: String = "",
        @SerializedName("email") val email: String = "",
        @SerializedName("admin_email") val adminEmail: String = "",
        @SerializedName("timestamp") val timestamp: String = "",
        @SerializedName("profile_picture") val profilePicture: String = ""
) : Comparable<Project>, Serializable, Cloneable {

    override fun compareTo(other: Project): Int = if (other.id == this.id) 0 else 1

    public override fun clone(): Any {
        return Project(this.id,
                this.projectName,
                this.projectDescription,
                this.eta,
                this.completion,
                this.assignedTo,
                this.email,
                this.adminEmail,
                this.timestamp,
                this.profilePicture)
    }
}