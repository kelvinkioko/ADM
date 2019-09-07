package com.download.manager.video.whatsapp.database.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.download.manager.video.whatsapp.database.DownloadDatabase
import com.download.manager.video.whatsapp.database.dao.InstaDao
import com.download.manager.video.whatsapp.database.entity.InstaEntity

class InstaRepository(application: Application) {

    private val db = DownloadDatabase.getDatabase(application)
    private val instaDao: InstaDao = db.instaDao()

    @WorkerThread
    fun insertInsta(instaEntity: InstaEntity) {
        instaDao.insertInsta(instaEntity)
    }

    fun getInsta(): LiveData<List<InstaEntity>>{
        return instaDao.getInsta()
    }

    fun getInstaList(): List<InstaEntity>{
        return instaDao.getInstaList()
    }

    fun countInstaList(): Int{
        return instaDao.countInstaList()
    }

    fun updateInsta(downloaded: String, size: String, id: Int){
        return instaDao.updateInsta(downloaded, size, id)
    }

    fun updateLocalURL(localurl: String, id: Int){
        return instaDao.updateLocalURL(localurl, id)
    }

    fun updateName(name: String, id: Int){
        return instaDao.updateName(name, id)
    }

    fun deleteInsta(){
        return instaDao.deleteInsta()
    }
}