package com.download.manager.video.whatsapp.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import android.content.Context
import com.download.manager.video.whatsapp.database.dao.BookmarkDao
import com.download.manager.video.whatsapp.database.dao.DownloadsDao
import com.download.manager.video.whatsapp.database.entity.BookmarkEntity
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity

@Database(entities = arrayOf(DownloadsEntity::class, BookmarkEntity::class), version = 1)
abstract class DownloadDatabase : RoomDatabase() {

    abstract fun downloadsDao(): DownloadsDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: DownloadDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `errandTrip` (`id` INTEGER NOT NULL, `trip_id` TEXT, `od_start` TEXT, `od_end` TEXT, PRIMARY KEY(`id`))")
                database.execSQL("ALTER TABLE `trip` ADD COLUMN `number_plate` TEXT")
            }
        }

        fun getDatabase(context: Context): DownloadDatabase {
            var tempInstance = INSTANCE
            if (tempInstance == null) {
                tempInstance = Room.databaseBuilder<DownloadDatabase>(context.applicationContext, DownloadDatabase::class.java!!, "downloads_database")
                    .allowMainThreadQueries()
                    .build()
            }
            return tempInstance
        }
    }
}