package com.krtechnologies.officemate.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.MembersDiffUtils
import com.krtechnologies.officemate.models.Employee
import com.krtechnologies.officemate.models.Member
import org.jetbrains.anko.toast

/**
 * This project is created by Kamran Ramzan on 17-Aug-18.
 */
class MembersAdapter(val context: Context, private val isContextual: Boolean) : RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    private var previousPosition: Int = 0
    private var employeesList: MutableList<Employee> = ArrayList()
    private var listener: ((employee: Employee) -> Unit)? = null

    fun setItemClickListener(listener: (employee: Employee) -> Unit) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_members, parent, false))


    override fun getItemCount(): Int {
        return employeesList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val employee = employeesList[position]

        holder.tvName.text = employee.name
        holder.tvDesignation.text = employee.designation

        Glide.with(context)
                .asBitmap()
                .load(employee.profilePicture)
                .apply(RequestOptions().override(Helper.getInstance().convertDpToPixel(60f).toInt(), Helper.getInstance().convertDpToPixel(60f).toInt()).fallback(R.drawable.person).error(R.drawable.person))
                .into(object : Target<Bitmap> {
                    override fun onLoadStarted(placeholder: Drawable?) {
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                    }

                    override fun getSize(cb: SizeReadyCallback) {
                    }

                    override fun getRequest(): Request? {
                        return null
                    }

                    override fun onStop() {
                    }

                    override fun setRequest(request: Request?) {
                    }

                    override fun removeCallback(cb: SizeReadyCallback) {
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                    override fun onStart() {
                    }

                    override fun onDestroy() {
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        holder.ivProfilePicture.setImageBitmap(resource)
                    }

                })

        if (employee.isChecked) {
            if (holder.ivChecked.visibility != View.VISIBLE)
                holder.ivChecked.visibility = View.VISIBLE
        } else {
            if (holder.ivChecked.visibility != View.GONE)
                holder.ivChecked.visibility = View.GONE
        }

        holder.item.setOnClickListener {
            if (listener != null) {
                listener!!(employee)
            }
            if (isContextual) {
                employee.isChecked = true
                if (holder.ivChecked.visibility != View.VISIBLE)
                    holder.ivChecked.visibility = View.VISIBLE
                if (previousPosition != holder.adapterPosition) {
                    val previousEmployee = employeesList[previousPosition]
                    previousEmployee.isChecked = false
                    notifyItemChanged(previousPosition)
                }
                previousPosition = holder.adapterPosition
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            TODO("Not implemented yet")
        }

    }

    fun updateList(newList: MutableList<Employee>) {
        val diffResult = DiffUtil.calculateDiff(MembersDiffUtils(newList, this.employeesList), true)
        diffResult.dispatchUpdatesTo(this)
        this.employeesList.clear()
        this.employeesList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivProfilePicture = view.findViewById<ImageView>(R.id.ivProfilePicture)!!
        val tvName = view.findViewById<TextView>(R.id.tvName)!!
        val tvDesignation = view.findViewById<TextView>(R.id.tvDesignation)!!
        val item = view.findViewById<ConstraintLayout>(R.id.item)!!
        val ivChecked = view.findViewById<ImageView>(R.id.ivChecked)!!

    }
}
