package com.download.manager.video.whatsapp.database.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.repository.DownloadsRepository

class DownloadsViewModel(application: Application) : AndroidViewModel(application) {

    private val downloadsRepo: DownloadsRepository = DownloadsRepository(application)

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
}