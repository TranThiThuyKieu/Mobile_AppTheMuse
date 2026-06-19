package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.data.remote.dto.BookDto
import com.example.appthemuse.domain.model.BookModel
import com.example.appthemuse.domain.model.CategoryModel

class BookRepository(private val firestoreService: FirestoreService) {

    // Lấy danh sách thể loại sạch cho Domain / UI
    suspend fun getCategories(): List<CategoryModel> {
        val dtos = firestoreService.getCategoriesList()
        return dtos.map { dto ->
            CategoryModel(
                id = dto.id,
                name = dto.name,
                imageUrl = "" // Sẵn sàng mở rộng logic hình ảnh
            )
        }
    }

    // Trả về danh sách truyện thịnh hành cấu trúc chuẩn sạch
    suspend fun getTrendingBooks(limit: Long = 5): Result<List<BookModel>> {
        return try {
            val dtos = firestoreService.getTrendingBooks(limit)
            val models = dtos.map { mapDtoToModel(it) }
            Result.success(models)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentBooks(limit: Long = 5): Result<List<BookModel>> {
        return try {
            val dtos = firestoreService.getRecentBooks(limit)
            val models = dtos.map { mapDtoToModel(it) }
            Result.success(models)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecommendedBooks(favoriteGenres: List<String>, limit: Long = 5): Result<List<BookModel>> {
        return try {
            val dtos = firestoreService.getRecommendedBooks(favoriteGenres, limit)
            val models = dtos.map { mapDtoToModel(it) }
            Result.success(models)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hàm bóc tách, ánh xạ chuyển đổi (Mapping) từ DTO thô sang Model hoàn chỉnh
    private suspend fun mapDtoToModel(dto: BookDto): BookModel {
        val authorName = firestoreService.getAuthorName(dto.author_id)
        val chapterCount = firestoreService.getChapterCount(dto.id)
        val rating = firestoreService.getAverageRating(dto.id)

        return BookModel(
            id = dto.id,
            title = dto.title,
            slug = dto.slug,
            authorId = dto.author_id,
            authorName = authorName,
            categoryName = dto.category_name,
            coverUrl = dto.cover_url,
            description = dto.description,
            isPremium = dto.is_premium,
            viewCount = dto.view_count,
            status = dto.status,
            chapterCount = chapterCount,
            rating = rating,
            createdAt = dto.created_at
        )
    }
}