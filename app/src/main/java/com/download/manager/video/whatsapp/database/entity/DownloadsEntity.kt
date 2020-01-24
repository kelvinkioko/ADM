package com.download.manager.video.whatsapp.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloadsEntity")
data class DownloadsEntity(

    @PrimaryKey(autoGenerate = true) var id: Int,

    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "url") var url: String,
    @ColumnInfo(name = "localurl") var localurl: String,
    @ColumnInfo(name = "downloaded") var downloaded: String,
    @ColumnInfo(name = "size") var size: String,
    @ColumnInfo(name = "datecreated") var datecreated: String

)