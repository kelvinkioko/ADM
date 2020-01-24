package com.download.manager.video.whatsapp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarksEntity")
data class BookmarkEntity(

    @PrimaryKey(autoGenerate = true) var id: Int,

    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "url") var url: String

)