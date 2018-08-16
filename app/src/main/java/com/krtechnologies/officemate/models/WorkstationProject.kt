package com.krtechnologies.officemate.models

import java.io.Serializable

/**
 * Created by ingizly on 8/15/18
 **/

data class WorkstationProject(val id: String, val projectName: String, val projectDescription: String) : Comparable<WorkstationProject>, Serializable {


    override fun compareTo(other: WorkstationProject): Int = if (other.id == this.id) 0 else 1


}