package com.example.appthemuse.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appthemuse.data.local.dao.DownloadedBookDao
import com.example.appthemuse.data.local.entity.DownloadedBookEntity
import com.example.appthemuse.data.local.entity.DownloadedChapterEntity

@Database(
    entities = [DownloadedBookEntity::class, DownloadedChapterEntity::class],
    version = 3, // Nâng cấp version để đồng bộ schema description
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadedBookDao(): DownloadedBookDao
}
