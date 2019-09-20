package com.example.pinch.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * @author Baptiste Cassar
 **/
@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true) val key: Int,
    @field:SerializedName("id") val gameId: Int,
    @field:SerializedName("name") val name: String? = "",
    @field:SerializedName("summary") val summary: String? = "",
    @field:SerializedName("updated_at") val updatedAt: Long
) {
    @Ignore
    val calendar = Calendar.getInstance().apply { timeInMillis = updatedAt * 1000 }
}