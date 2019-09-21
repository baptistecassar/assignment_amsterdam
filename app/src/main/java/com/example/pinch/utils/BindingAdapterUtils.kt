package com.example.pinch.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter


/**
 * @author Baptiste Cassar
 **/
object BindingAdapterUtils {

    /**
     * data binding method to load image
     * if the imageUrl is not empty load the image
     * otherwise load the default image
     */
    @BindingAdapter(value = ["imageUrl", "errorDrawable", "defaultDrawable"], requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(
        imageUrl: String?,
        errorDrawable: Drawable? = null,
        defaultDrawable: Drawable? = null
    ) {
        ImageUtils.loadImage(this, imageUrl, errorDrawable, defaultDrawable)
    }
}