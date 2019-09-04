package com.download.manager.video.whatsapp.database

import android.app.Application
import android.content.Context
import com.download.manager.video.whatsapp.database.dao.DownloadsDao

class DatabaseApp: Application() {

    private lateinit var downloadsDao: DownloadsDao
    private lateinit var instance: DatabaseApp
    private val TAG = "downloadApp"

    override fun onCreate() {
        super.onCreate()
        instance = this
        downloadsDao = DownloadDatabase.getDatabase(this).downloadsDao()
    }

    @Synchronized
    fun getDownloadsDao(context: Context): DownloadsDao {
        downloadsDao = DownloadDatabase.getDatabase(context.applicationContext).downloadsDao()
        return downloadsDao
    }
}