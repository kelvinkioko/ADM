package com.download.manager.video.whatsapp.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "bookmarksEntity")
data class BookmarkEntity(

    @PrimaryKey(autoGenerate = true) var id: Int,

    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "url") var url: String

)