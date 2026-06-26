package com.example.appthemuse.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.appthemuse.data.local.dao.DownloadedBookDao
import com.example.appthemuse.data.local.entity.DownloadedBookEntity

@Database(
    entities = [DownloadedBookEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun downloadedBookDao():
            DownloadedBookDao
}