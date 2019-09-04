package com.download.manager.video.whatsapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity

@Dao
interface DownloadsDao {

    @Insert
    fun insertDownloads(vararg downloads: DownloadsEntity)

    @Query("SELECT * FROM downloadsEntity ORDER BY id DESC")
    fun getDownloads(): LiveData<List<DownloadsEntity>>

    @Query("UPDATE downloadsEntity SET downloaded =:downloaded WHERE id =:id")
    fun updateDownloads(downloaded: String, id: Int)

    @Query("DELETE FROM downloadsEntity")
    fun deleteDownloads()
}