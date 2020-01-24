package com.download.manager.video.whatsapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity

@Dao
interface DownloadsDao {

    @Insert
    fun insertDownloads(vararg downloads: DownloadsEntity)

    @Query("SELECT * FROM downloadsEntity ORDER BY id DESC")
    fun getDownloads(): LiveData<List<DownloadsEntity>>

    @Query("SELECT * FROM downloadsEntity ORDER BY id DESC")
    fun getDownloadsList(): List<DownloadsEntity>

    @Query("UPDATE downloadsEntity SET downloaded =:downloaded, size =:size WHERE id =:id")
    fun updateDownloads(downloaded: String, size: String, id: Int)

    @Query("UPDATE downloadsEntity SET localurl =:localurl WHERE id =:id")
    fun updateLocalURL(localurl: String, id: Int)

    @Query("UPDATE downloadsEntity SET name =:name WHERE id =:id")
    fun updateName(name: String, id: Int)

    @Query("SELECT COUNT(id) FROM downloadsEntity WHERE url =:url")
    fun countDownload(url: String): Int

    @Query("SELECT COUNT(id) FROM downloadsEntity WHERE localurl =:local_url")
    fun countDownloadsByUrl(local_url: String): Int

    @Query("SELECT COUNT(id) FROM downloadsEntity")
    fun countDownloads(): Int

    @Query("SELECT * FROM downloadsEntity WHERE url =:url")
    fun getDownloadByUrl(url: String): DownloadsEntity

    @Query("DELETE FROM downloadsEntity WHERE id =:id")
    fun deleteDownloadsByID(id: Int)

    @Query("DELETE FROM downloadsEntity")
    fun deleteDownloads()
}