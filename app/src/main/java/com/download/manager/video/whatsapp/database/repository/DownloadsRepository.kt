package com.download.manager.video.whatsapp.database.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.download.manager.video.whatsapp.database.DownloadDatabase
import com.download.manager.video.whatsapp.database.dao.DownloadsDao
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity

class DownloadsRepository(application: Application) {

    private val db = DownloadDatabase.getDatabase(application)
    private val downloadsDao: DownloadsDao = db.downloadsDao()

    @WorkerThread
    fun insertDownloads(downloadsEntity: DownloadsEntity) {
        downloadsDao.insertDownloads(downloadsEntity)
    }

    fun getDownloads(): LiveData<List<DownloadsEntity>> {
        return downloadsDao.getDownloads()
    }

    fun updateDownloads(downloaded: String, id: Int){
        downloadsDao.updateDownloads(downloaded, id)
    }

    fun deleteDownloads(){
        downloadsDao.deleteDownloads()
    }
}