package com.example.project.model

import com.google.gson.annotations.SerializedName

/**
 * @author Baptiste Cassar
 * data class used to get the links to a games' images
 **/
data class Cover(
    @SerializedName("id") val id: Int,
    @SerializedName("image_id") val imageId: String?
)