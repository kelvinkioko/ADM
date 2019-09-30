package com.download.manager.video.whatsapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.download.manager.video.whatsapp.database.entity.BookmarkEntity
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity

@Dao
interface BookmarkDao {

    @Insert
    fun insertBookmark(vararg downloads: BookmarkEntity)

    @Query("SELECT * FROM bookmarksEntity ORDER BY id DESC")
    fun getBookmark(): LiveData<List<BookmarkEntity>>

    @Query("SELECT COUNT(id) FROM bookmarksEntity")
    fun countBookmark(): Int

    @Query("DELETE FROM bookmarksEntity")
    fun deleteBookmark()
}