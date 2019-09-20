package com.example.pinch.model

import com.google.gson.annotations.SerializedName

/**
 * @author Baptiste Cassar
 * @date 2019-09-20
 **/
data class Cover(
    @SerializedName("id") val id: Int,
    @SerializedName("image_id") val imageId: String?
)