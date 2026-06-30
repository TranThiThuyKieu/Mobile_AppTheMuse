package com.example.appthemuse.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appthemuse.data.local.dao.DownloadedBookDao
import com.example.appthemuse.data.local.entity.DownloadedBookEntity
import com.example.appthemuse.data.local.entity.DownloadedChapterEntity

@Database(
    entities = [DownloadedBookEntity::class, DownloadedChapterEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Khởi tạo đối tượng DAO cho cơ sở dữ liệu
    abstract fun downloadedBookDao(): DownloadedBookDao
}