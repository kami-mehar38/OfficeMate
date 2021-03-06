package com.krtechnologies.officemate.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.transition.Transition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.MessagesDiffUtils
import com.krtechnologies.officemate.models.Message
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.find

/**
 * Created by ingizly on 9/1/18
 **/

class MessagesAdapter(val context: Context) : RecyclerView.Adapter<MessagesAdapter.ViewHolder>() {

    private var messagesList: MutableList<Message> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            0 -> ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false))
            else -> ViewHolder(LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false))
        }
    }


    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun getItemViewType(position: Int): Int {
        return messagesList[position].isMine
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        Glide.with(context)
                .asBitmap()
                .load(R.drawable.person)
                .apply(RequestOptions().override(Helper.getInstance().convertDpToPixel(40f).toInt(), Helper.getInstance().convertDpToPixel(40f).toInt()).fallback(R.drawable.person).error(R.drawable.person))
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

                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                        holder.ivProfilePicture.setImageBitmap(resource)
                    }

                })


    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty())
            super.onBindViewHolder(holder, position, payloads)
        else {
            TODO("Not implemented yet")
        }
    }

    fun updateList(newList: MutableList<Message>) {
        val diffResult = DiffUtil.calculateDiff(MessagesDiffUtils(newList, this.messagesList), true)
        diffResult.dispatchUpdatesTo(this)
        this.messagesList.clear()
        this.messagesList.addAll(newList)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivProfilePicture = view.find<CircleImageView>(R.id.ivProfilePicture)
    }
}