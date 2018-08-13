package com.krtechnologies.officemate.models

/**
 * This project is created by Kamran Ramzan on 13-Aug-18.
 */

data class NewsFeed(val id: String, val name: String) : Comparable<NewsFeed> {


    override fun compareTo(other: NewsFeed): Int {
        return 0
    }

}