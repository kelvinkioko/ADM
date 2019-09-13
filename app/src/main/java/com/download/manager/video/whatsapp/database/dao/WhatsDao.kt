package com.download.manager.video.whatsapp.database.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.download.manager.video.whatsapp.database.entity.WhatsEntity

@Dao
interface WhatsDao {

    @Insert
    fun insertWhats(vararg whats: WhatsEntity)

    @Query("SELECT * FROM whatsEntity ORDER BY timestamp DESC")
    fun getWhats(): LiveData<List<WhatsEntity>>

    @Query("SELECT * FROM whatsEntity ORDER BY id DESC")
    fun getWhatsList(): List<WhatsEntity>

    @Query("SELECT COUNT(id) FROM whatsEntity WHERE liveUri =:name")
    fun countWhatsListByName(name: String): Int

    @Query("SELECT COUNT(id) FROM whatsEntity WHERE liveUri =:name AND status ='downloaded'")
    fun countWhatsListByNameAndDownloaded(name: String): Int

    @Query("SELECT COUNT(id) FROM whatsEntity")
    fun countWhatsList(): Int

    @Query("UPDATE whatsEntity SET size =:size WHERE id =:id")
    fun updateWhats(size: String, id: Int)

    @Query("UPDATE whatsEntity SET localurl =:localurl, status ='downloaded' WHERE id =:id")
    fun updateLocalURL(localurl: String, id: Int)

    @Query("UPDATE whatsEntity SET name =:name WHERE id =:id")
    fun updateName(name: String, id: Int)

    @Query("DELETE FROM whatsEntity WHERE liveUri =:name AND status ='live'")
    fun deleteWhatsListByNameAndDownloaded(name: String)

    @Query("DELETE FROM whatsEntity WHERE id =:id")
    fun deleteWhatsById(id: Int)

    @Query("DELETE FROM whatsEntity")
    fun deleteWhats()
}