package com.download.manager.video.whatsapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.download.manager.video.whatsapp.database.entity.InstaEntity

@Dao
interface InstaDao {

    @Insert
    fun insertInsta(vararg downloads: InstaEntity)

    @Query("SELECT * FROM instaEntity ORDER BY id DESC")
    fun getInsta(): LiveData<List<InstaEntity>>

    @Query("SELECT * FROM instaEntity ORDER BY id DESC")
    fun getInstaList(): List<InstaEntity>

    @Query("SELECT COUNT(id) FROM instaEntity")
    fun countInstaList(): Int

    @Query("UPDATE instaEntity SET size =:size WHERE id =:id")
    fun updateInsta(size: String, id: Int)

    @Query("UPDATE instaEntity SET localurl =:localurl WHERE id =:id")
    fun updateLocalURL(localurl: String, id: Int)

    @Query("UPDATE instaEntity SET name =:name WHERE id =:id")
    fun updateName(name: String, id: Int)

    @Query("DELETE FROM instaEntity")
    fun deleteInsta()
}