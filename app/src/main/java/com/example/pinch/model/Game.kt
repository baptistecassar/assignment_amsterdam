package com.example.pinch.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * @author Baptiste Cassar
 **/
@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val key: Int,
    @field:SerializedName("id") val gameId: Int,
    @field:SerializedName("cover") val coverId: Int? = null,
    @field:SerializedName("name") val name: String? = "",
    @field:SerializedName("summary") val summary: String? = "",
    @field:SerializedName("updated_at") val updatedAt: Long? = null,
    @field:SerializedName("thumbnailUrl") var thumbnailUrl: String? = "",
    @field:SerializedName("coverUrl") var coverUrl: String? = ""
) : Serializable