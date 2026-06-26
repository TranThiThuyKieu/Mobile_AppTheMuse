package com.example.appthemuse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.appthemuse.data.local.entity.DownloadedBookEntity

@Dao
interface DownloadedBookDao {
    // Thêm một sách đã tải xuống mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(
        book: DownloadedBookEntity
    ): Long
    // Lấy toàn bộ danh sách sách đã tải xuống từ database
    @Query("SELECT * FROM downloaded_books")
    suspend fun getAllBooks():
            List<DownloadedBookEntity>

}