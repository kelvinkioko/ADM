package com.download.manager.video.whatsapp.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "faceEntity")
data class FaceEntity(

    @PrimaryKey(autoGenerate = true) var id: Int,

    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "postedBy") var postedBy: String,
    @ColumnInfo(name = "imageUrl") var imageUrl: String,
    @ColumnInfo(name = "videoUrl") var videoUrl: String,
    @ColumnInfo(name = "parentUrl") var parentUrl: String,
    @ColumnInfo(name = "localUrl") var localUrl: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "downloaded") var downloaded: String,
    @ColumnInfo(name = "size") var size: String,
    @ColumnInfo(name = "datecreated") var datecreated: String

)