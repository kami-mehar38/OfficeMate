package com.krtechnologies.officemate.helpers

import android.content.Context
import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.krtechnologies.officemate.R


/**
 * This project is created by Kamran Ramzan on 24-Aug-18.
 */

class SimpleDividerItemDecoration(private val context: Context) : RecyclerView.ItemDecoration() {
    private var mDivider: Drawable? = null

    init {
        mDivider = ContextCompat.getDrawable(context, R.drawable.divider)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft.plus(Helper.getInstance().convertDpToPixel(93f)).toInt()
        val right = (parent.width - parent.paddingRight).minus(Helper.getInstance().convertDpToPixel(8f)).toInt()

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.getIntrinsicHeight()

            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }
    }
}