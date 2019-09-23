package com.example.project.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.example.project.R
import com.squareup.picasso.Picasso

/**
 * @author Baptiste Cassar
 * class used for all methods linked to images
 **/
object ImageUtils {

    /**
     * loads image with an url using [Picasso]
     * before loading [defaultDrawable] or [R.color.gray_light] is used
     * if an error occurs [errorDrawable] is used
     */
    @JvmStatic
    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        errorDrawable: Drawable?,
        defaultDrawable: Drawable?
    ) {
        val creator = Picasso.get()
            .load(imageUrl)
        if (errorDrawable != null)
            creator.error(errorDrawable)
        if (defaultDrawable != null)
            creator.placeholder(defaultDrawable)
        else
            creator.placeholder(R.color.gray_light)
        creator.fit()
            .centerInside()
            .into(imageView)
    }
}