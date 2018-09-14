package com.krtechnologies.officemate.fragments


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.krtechnologies.officemate.LoginActivity
import com.krtechnologies.officemate.ProfileSettingsActivity
import com.krtechnologies.officemate.R
import com.krtechnologies.officemate.helpers.Helper
import com.krtechnologies.officemate.helpers.PreferencesManager
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.Serializable
import org.jetbrains.anko.startActivity


class SettingsFragment : Fragment(), Serializable {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tvName.text = PreferencesManager.getInstance().getUserName()
        tvDesignation.text = PreferencesManager.getInstance().getUserDesignation()

        ivProfilePicture.setOnClickListener {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, ivProfilePicture as View, "ivProfilePicture")
            val intent = Intent(context!!, ProfileSettingsActivity::class.java)
            startActivity(intent, options.toBundle())
        }

        btnSignOut.setOnClickListener {
            startActivity(Intent(context, LoginActivity::class.java))
            PreferencesManager.getInstance().setLogInStatus(false)
        }

        Glide.with(this)
                .asBitmap()
                .load(PreferencesManager.getInstance().getProfilePicture())
                .apply(RequestOptions().override(Helper.getInstance().convertDpToPixel(100f).toInt(), Helper.getInstance().convertDpToPixel(100f).toInt()).fallback(R.drawable.person).error(R.drawable.person))
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
                        ivProfilePicture.setImageBitmap(resource)
                    }

                })
    }
}
