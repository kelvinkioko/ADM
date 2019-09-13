package com.download.manager.video.whatsapp.database.repository

import android.app.Application
import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.download.manager.video.whatsapp.database.DownloadDatabase
import com.download.manager.video.whatsapp.database.dao.FaceDao
import com.download.manager.video.whatsapp.database.entity.FaceEntity

class FaceRepository(application: Application) {

    private val db = DownloadDatabase.getDatabase(application)
    private val faceDao: FaceDao = db.faceDao()

    @WorkerThread
    fun insertFace(faceEntity: FaceEntity) {
        faceDao.insertFace(faceEntity)
    }

    fun getFace(): LiveData<List<FaceEntity>>{
        return faceDao.getFace()
    }

    fun getFaceList(): List<FaceEntity>{
        return faceDao.getFaceList()
    }

    fun countFaceList(): Int{
        return faceDao.countFaceList()
    }

    fun updateFace(downloaded: String, size: String, id: Int){
        return faceDao.updateFace(downloaded, size, id)
    }

    fun updateLocalFaceURL(localurl: String, id: Int){
        return faceDao.updateLocalURL(localurl, id)
    }

    fun updateFaceName(name: String, id: Int){
        return faceDao.updateName(name, id)
    }

    fun deleteFace(){
        return faceDao.deleteFace()
    }
}