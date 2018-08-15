package com.krtechnologies.officemate.models

/**
 * Created by ingizly on 8/15/18
 **/

data class WorkstationProject(val id: String) : Comparable<WorkstationProject> {


    override fun compareTo(other: WorkstationProject): Int = if (other.id == this.id) 0 else 1


}