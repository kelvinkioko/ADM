package com.download.manager.video.whatsapp.database.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.download.manager.video.whatsapp.database.DownloadDatabase
import com.download.manager.video.whatsapp.database.dao.WhatsDao
import com.download.manager.video.whatsapp.database.entity.WhatsEntity

class WhatsRepository(application: Application) {

    private val db = DownloadDatabase.getDatabase(application)
    private val whatsDao: WhatsDao = db.whatsDao()

    @WorkerThread
    fun insertWhats(whatsEntity: WhatsEntity) {
        whatsDao.insertWhats(whatsEntity)
    }

    fun getWhats(): LiveData<List<WhatsEntity>>{
        return whatsDao.getWhats()
    }

    fun getWhatsList(): List<WhatsEntity>{
        return whatsDao.getWhatsList()
    }

    fun countWhatsList(): Int{
        return whatsDao.countWhatsList()
    }

    fun updateWhats(size: String, id: Int){
        return whatsDao.updateWhats(size, id)
    }

    fun updateLocalURL(localurl: String, id: Int){
        return whatsDao.updateLocalURL(localurl, id)
    }

    fun updateName(name: String, id: Int){
        return whatsDao.updateName(name, id)
    }

    fun deleteWhats(){
        return whatsDao.deleteWhats()
    }
}