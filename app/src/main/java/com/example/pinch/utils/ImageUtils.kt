package com.example.pinch.utils

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.example.pinch.R
import com.squareup.picasso.Picasso

/**
 * @author Baptiste Cassar
 * @date 15/12/2018
 **/
object ImageUtils {

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