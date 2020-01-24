package com.download.manager.video.whatsapp.database.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread
import com.download.manager.video.whatsapp.database.entity.BookmarkEntity
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

    fun countDownloadsByUrl(local_url: String): Int{
        return downloadsRepo.countDownloadsByUrl(local_url)
    }

    fun countDownloads(): Int{
        return downloadsRepo.countDownloads()
    }

    fun deleteDownloads(){
        downloadsRepo.deleteDownloads()
    }


    /**
     *
     */
    fun insertBookmark(bookmark: BookmarkEntity) {
        downloadsRepo.insertBookmark(bookmark)
    }

    fun getBookmark(): LiveData<List<BookmarkEntity>> {
        return downloadsRepo.getBookmark()
    }

    fun countBookmark(): Int{
        return downloadsRepo.countBookmark()
    }

    fun deleteBookmark(){
        downloadsRepo.deleteBookmark()
    }

}