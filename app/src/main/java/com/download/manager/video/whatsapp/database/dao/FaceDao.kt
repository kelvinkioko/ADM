package com.download.manager.video.whatsapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.download.manager.video.whatsapp.database.entity.FaceEntity

@Dao
interface FaceDao {

    @Insert
    fun insertFace(vararg face: FaceEntity)

    @Query("SELECT * FROM faceEntity ORDER BY id DESC")
    fun getFace(): LiveData<List<FaceEntity>>

    @Query("SELECT * FROM faceEntity ORDER BY id DESC")
    fun getFaceList(): List<FaceEntity>

    @Query("SELECT COUNT(id) FROM faceEntity")
    fun countFaceList(): Int

    @Query("UPDATE faceEntity SET downloaded =:downloaded, size =:size WHERE id =:id")
    fun updateFace(downloaded: String, size: String, id: Int)

    @Query("UPDATE faceEntity SET localurl =:localurl WHERE id =:id")
    fun updateLocalURL(localurl: String, id: Int)

    @Query("UPDATE faceEntity SET name =:name WHERE id =:id")
    fun updateName(name: String, id: Int)

    @Query("DELETE FROM faceEntity")
    fun deleteFace()
}