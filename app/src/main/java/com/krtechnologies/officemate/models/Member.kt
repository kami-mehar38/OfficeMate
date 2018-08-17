package com.krtechnologies.officemate.models

/**
 * This project is created by Kamran Ramzan on 17-Aug-18.
 */

data class Member(val id: String, val name: String, val designation: String) : Comparable<Member> {

    override fun compareTo(other: Member): Int = if (other.id == this.id) 0 else 1

}