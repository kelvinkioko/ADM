package com.download.manager.video.whatsapp.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "instaEntity")
data class InstaEntity(

    @PrimaryKey(autoGenerate = true) var id: Int,

    @ColumnInfo(name = "postedBy") var postedBy: String,
    @ColumnInfo(name = "image") var image: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "tempUrl") var tempUrl: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "video") var video: String

)