package com.download.manager.video.whatsapp.database.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "whatsEntity")
data class WhatsEntity(

    @PrimaryKey(autoGenerate = true) var id: Int,

    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "liveUrl") var liveUrl: String,
    @ColumnInfo(name = "liveUri") var liveUri: String,
    @ColumnInfo(name = "localUrl") var localUrl: String,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "type") var type: String,
    @ColumnInfo(name = "size") var size: String,
    @ColumnInfo(name = "timestamp") var timestamp: String,
    @ColumnInfo(name = "datecreated") var datecreated: String

)