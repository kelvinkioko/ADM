package com.download.manager.video.whatsapp.database

import android.app.Application
import android.content.Context
import com.download.manager.video.whatsapp.database.dao.DownloadsDao
import com.download.manager.video.whatsapp.database.dao.InstaDao
import com.download.manager.video.whatsapp.database.dao.WhatsDao

class DatabaseApp: Application() {

    private lateinit var downloadsDao: DownloadsDao
    private lateinit var instaDao: InstaDao
    private lateinit var whatsDao: WhatsDao
    private lateinit var instance: DatabaseApp
    private val TAG = "downloadApp"

    override fun onCreate() {
        super.onCreate()
        instance = this
        downloadsDao = DownloadDatabase.getDatabase(this).downloadsDao()
        instaDao = DownloadDatabase.getDatabase(this).instaDao()
    }

    @Synchronized
    fun getDownloadsDao(context: Context): DownloadsDao {
        downloadsDao = DownloadDatabase.getDatabase(context.applicationContext).downloadsDao()
        return downloadsDao
    }

    @Synchronized
    fun getInstaDao(context: Context): InstaDao {
        instaDao = DownloadDatabase.getDatabase(context.applicationContext).instaDao()
        return instaDao
    }

    @Synchronized
    fun getWhatsDao(context: Context): WhatsDao {
        whatsDao = DownloadDatabase.getDatabase(context.applicationContext).whatsDao()
        return whatsDao
    }
}