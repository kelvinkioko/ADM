package com.download.manager.video.whatsapp.database.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.database.repository.DownloadsRepository
import com.download.manager.video.whatsapp.database.repository.InstaRepository

class DownloadsViewModel(application: Application) : AndroidViewModel(application) {

    private val downloadsRepo: DownloadsRepository = DownloadsRepository(application)
    private val instaRepo: InstaRepository = InstaRepository(application)

    fun insertDownloads(downloadsEntity: DownloadsEntity){
        downloadsRepo.insertDownloads(downloadsEntity)
    }

    fun getDownloads(): LiveData<List<DownloadsEntity>> {
        return downloadsRepo.getDownloads()
    }

    fun updateDownloads(downloaded: String, size: String, id: Int){
        downloadsRepo.updateDownloads(downloaded, size, id)
    }

    fun deleteDownloads(){
        downloadsRepo.deleteDownloads()
    }

    /**
     * Insta repo functions
     */
    @WorkerThread
    fun insertInsta(instaEntity: InstaEntity) {
        instaRepo.insertInsta(instaEntity)
    }

    fun getInsta(): LiveData<List<InstaEntity>>{
        return instaRepo.getInsta()
    }

    fun getInstaList(): List<InstaEntity>{
        return instaRepo.getInstaList()
    }

    fun countInstaList(): Int{
        return instaRepo.countInstaList()
    }

    fun updateInsta(size: String, id: Int){
        return instaRepo.updateInsta(size, id)
    }

    fun updateLocalURL(localurl: String, id: Int){
        return instaRepo.updateLocalURL(localurl, id)
    }

    fun updateName(name: String, id: Int){
        return instaRepo.updateName(name, id)
    }

    fun deleteInsta(){
        return instaRepo.deleteInsta()
    }
}