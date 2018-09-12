package com.krtechnologies.officemate.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.ContactsDiffUtils
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.NewsFeedDiffUtils
import com.krtechnologies.officemate.models.Contact
import com.krtechnologies.officemate.models.Project

/**
 * Created by ingizly on 9/12/18
 **/
class ContactsAdapter(val context: Context) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    private var projectsList: MutableList<Contact> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsAdapter.ViewHolder = ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_contact, parent, false))


    override fun getItemCount(): Int {
        return projectsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val contact = projectsList[position]
        holder.tvName.text = contact.name
        holder.tvPhoneNo.text = contact.phoneNo

        /*Glide.with(context)
                .asBitmap()
                .load(project.profilePicture)
                .apply(RequestOptions().override(Helper.getInstance().convertDpToPixel(50f).toInt(), Helper.getInstance().convertDpToPixel(50f).toInt()).fallback(R.drawable.person).error(R.drawable.person))
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

                })*/

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            TODO("Not implemented yet")
        }
    }

    fun updateList(newList: List<Contact>) {
        val diffResult = DiffUtil.calculateDiff(ContactsDiffUtils(newList, this.projectsList), true)
        diffResult.dispatchUpdatesTo(this)
        this.projectsList.clear()
        this.projectsList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.tvName)!!
        val tvPhoneNo = view.findViewById<TextView>(R.id.tvPhoneNo)!!
    }
}