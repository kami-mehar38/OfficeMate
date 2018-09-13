package com.krtechnologies.officemate.models

import java.io.Serializable

/**
 * This project is created by Kamran Ramzan on 13-Sep-18.
 */
data class File(val fileName: String, val fileSize: String) : Comparable<File>, Serializable {
    override fun compareTo(other: File): Int = if (other.fileName == this.fileName) 0 else 1
    override fun toString(): String {
        return "File(fileName='$fileName', fileSize='$fileSize')"
    }
}