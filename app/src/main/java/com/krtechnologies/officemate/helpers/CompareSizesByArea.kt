package com.krtechnologies.officemate.helpers

import android.annotation.TargetApi
import android.os.Build
import android.util.Size
import java.lang.Long.signum

import java.util.Comparator

/**
 * Compares two `Size`s based on their areas.
 */
internal class CompareSizesByArea : Comparator<Size> {

    // We cast here to ensure the multiplications won't overflow
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun compare(lhs: Size, rhs: Size) =
            signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

}
