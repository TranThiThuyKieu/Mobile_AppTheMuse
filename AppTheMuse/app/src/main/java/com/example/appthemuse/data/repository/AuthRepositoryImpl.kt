package com.example.appthemuse.data.repository

import com.example.appthemuse.data.remote.AuthService
import com.example.appthemuse.data.remote.FirestoreService
import com.example.appthemuse.domain.model.User
import com.example.appthemuse.domain.repository.AuthRepository
import com.google.firebase.Timestamp
import com.example.appthemuse.ui.mapper.mapDocumentToUser
import java.util.Calendar

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val firestoreService: FirestoreService
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // 1. Kiểm tra tài khoản có bị khóa không
            val lockStatus = isAccountLocked(email)
            if (lockStatus.getOrDefault(false)) {
                return Result.failure(Exception("Tài khoản của bạn đã bị khóa do đăng nhập sai quá nhiều lần. Vui lòng quay lại sau 24h."))
            }

            try {
                val firebaseUser = authService.loginWithEmail(email, password)
                if (firebaseUser != null) {
                    val doc = firestoreService.getUserDocument(firebaseUser.uid)
                    
                    // Reset số lần đăng nhập sai khi thành công
                    firestoreService.updateSecurityLog(email, mapOf("failed_attempts" to 0))
                    
                    Result.success(mapDocumentToUser(firebaseUser.uid, doc, firebaseUser.email ?: email))
                } else {
                    Result.failure(Exception("Đăng nhập thất bại."))
                }
            } catch (e: Exception) {
                // 2. Ghi nhận lần đăng nhập sai (Giới hạn 5 lần)
                val currentLog = firestoreService.getSecurityLog(email)
                val attempts = (currentLog.getLong("failed_attempts") ?: 0L) + 1
                val updateData = mutableMapOf<String, Any>("failed_attempts" to attempts)
                
                if (attempts >= 5) {
                    updateData["is_locked"] = true
                    updateData["locked_at"] = Timestamp.now()
                    firestoreService.updateSecurityLog(email, updateData)
                    return Result.failure(Exception("Bạn đã nhập sai 5 lần. Tài khoản đã bị khóa 24h để bảo mật!"))
                }
                
                firestoreService.updateSecurityLog(email, updateData)
                Result.failure(Exception("Mật khẩu không chính xác. Bạn còn ${5 - attempts} lần thử."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, username: String): Result<User> {
        return try {
            val firebaseUser = authService.registerWithEmail(email, password)
            if (firebaseUser != null) {
                val userData = mapOf(
                    "id" to firebaseUser.uid,
                    "username" to username,
                    "email" to email,
                    "role" to "user",
                    "is_blocked" to true, // Khóa tạm thời cho đến khi verify email
                    "favorite_genres" to emptyList<String>(),
                    "created_at" to Timestamp.now()
                )
                firestoreService.saveUserDocument(firebaseUser.uid, userData)
                
                // Khởi tạo log bảo mật cho email này
                firestoreService.updateSecurityLog(email, mapOf(
                    "failed_attempts" to 0,
                    "resend_count" to 1,
                    "last_resend_time" to Timestamp.now()
                ))

                Result.success(User(id = firebaseUser.uid, username = username, email = email))
            } else {
                Result.failure(Exception("Đăng ký thất bại."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkUserGenresSelected(userId: String): Result<Boolean> {
        return try {
            val doc = firestoreService.getUserDocument(userId)
            val genresRaw = (doc.get("favorite_genres") ?: doc.get("thể_loại_yêu_thích")) as? List<*>
            Result.success(!genresRaw.isNullOrEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveFavoriteGenres(userId: String, genres: List<String>): Result<Unit> {
        return try {
            firestoreService.updateFavoriteGenres(userId, genres)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        return try {
            val firebaseUser = authService.loginWithGoogle(idToken)
            if (firebaseUser != null) {
                var doc = firestoreService.getUserDocument(firebaseUser.uid)
                if (!doc.exists()) {
                    val userData = mapOf(
                        "id" to firebaseUser.uid,
                        "username" to (firebaseUser.displayName ?: "Người dùng Google"),
                        "email" to (firebaseUser.email ?: ""),
                        "role" to "user",
                        "is_blocked" to false,
                        "favorite_genres" to emptyList<String>(),
                        "created_at" to Timestamp.now()
                    )
                    firestoreService.saveUserDocument(firebaseUser.uid, userData)
                    doc = firestoreService.getUserDocument(firebaseUser.uid)
                }
                Result.success(mapDocumentToUser(firebaseUser.uid, doc, firebaseUser.email ?: ""))
            } else {
                Result.failure(Exception("Đăng nhập Google thất bại."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return authService.getCurrentUserId()
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val userId = authService.getCurrentUserId() ?: return Result.failure(Exception("Không tìm thấy người dùng."))
            val userDoc = firestoreService.getUserDocument(userId)
            val email = userDoc.getString("email") ?: ""
            
            // ✅ GIỚI HẠN GỬI LẠI MÃ (5 lần/24h)
            val limitCheck = checkResendVerificationLimit(email)
            if (limitCheck.isFailure) return Result.failure(limitCheck.exceptionOrNull()!!)
            
            authService.sendEmailVerification()
            
            // Cập nhật số lần gửi
            val log = firestoreService.getSecurityLog(email)
            val count = (log.getLong("resend_count") ?: 0L) + 1
            firestoreService.updateSecurityLog(email, mapOf(
                "resend_count" to count,
                "last_resend_time" to Timestamp.now()
            ))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isEmailVerified(): Boolean {
        val verified = authService.isEmailVerified()
        if (verified) {
            authService.getCurrentUserId()?.let { firestoreService.updateUserBlockStatus(it, false) }
        }
        return verified
    }

    // ✅ XÓA TÀI KHOẢN TRIỆT ĐỂ (Khi hết giờ xác minh)
    override suspend fun deleteUnverifiedAccount(userId: String) {
        try {
            firestoreService.deleteUserDocument(userId)
            authService.deleteCurrentUser()
        } catch (e: Exception) {
            android.util.Log.e("AuthRepositoryImpl", "Lỗi xóa tài khoản dở dang: ${e.message}")
        }
    }

    override suspend fun updateUserBlockStatus(userId: String, isBlocked: Boolean): Result<Unit> {
        return try {
            firestoreService.updateUserBlockStatus(userId, isBlocked)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ QUÊN MẬT KHẨU VỚI GIỚI HẠN 5 LẦN
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            val limitCheck = checkPasswordResetLimit(email)
            if (limitCheck.isFailure) return Result.failure(limitCheck.exceptionOrNull()!!)

            authService.sendPasswordResetEmail(email)
            
            val log = firestoreService.getSecurityLog(email)
            val count = (log.getLong("pw_reset_count") ?: 0L) + 1
            val updateData = mutableMapOf<String, Any>(
                "pw_reset_count" to count,
                "last_pw_reset_time" to Timestamp.now()
            )
            
            // Nếu gửi reset quá 5 lần -> Khóa luôn tài khoản
            if (count >= 5) {
                updateData["is_locked"] = true
                updateData["locked_at"] = Timestamp.now()
            }
            
            firestoreService.updateSecurityLog(email, updateData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkPasswordResetLimit(email: String): Result<Boolean> {
        return try {
            val log = firestoreService.getSecurityLog(email)
            if (!log.exists()) return Result.success(true)

            val count = log.getLong("pw_reset_count") ?: 0L
            val lastTime = log.getTimestamp("last_pw_reset_time")

            if (count >= 5) {
                if (lastTime != null) {
                    val cal = Calendar.getInstance()
                    cal.time = lastTime.toDate()
                    cal.add(Calendar.HOUR, 24)
                    if (Timestamp.now().toDate().before(cal.time)) {
                        return Result.failure(Exception("Bạn đã yêu cầu đặt lại mật khẩu quá 5 lần. Tài khoản đã bị khóa để bảo mật."))
                    } else {
                        firestoreService.updateSecurityLog(email, mapOf("pw_reset_count" to 0))
                    }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    override suspend fun checkResendVerificationLimit(email: String): Result<Boolean> {
        return try {
            val log = firestoreService.getSecurityLog(email)
            if (!log.exists()) return Result.success(true)

            val count = log.getLong("resend_count") ?: 0L
            val lastTime = log.getTimestamp("last_resend_time")

            if (count >= 5) {
                if (lastTime != null) {
                    val cal = Calendar.getInstance()
                    cal.time = lastTime.toDate()
                    cal.add(Calendar.HOUR, 24)
                    if (Timestamp.now().toDate().before(cal.time)) {
                        return Result.failure(Exception("Tính năng gửi lại mã bị khóa trong 24h do gửi quá 5 lần."))
                    } else {
                        firestoreService.updateSecurityLog(email, mapOf("resend_count" to 0))
                    }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.success(true)
        }
    }

    override suspend fun isAccountLocked(email: String): Result<Boolean> {
        return try {
            val log = firestoreService.getSecurityLog(email)
            if (!log.exists()) return Result.success(false)
            
            val isLocked = log.getBoolean("is_locked") ?: false
            if (isLocked) {
                val lockedAt = log.getTimestamp("locked_at")
                if (lockedAt != null) {
                    val cal = Calendar.getInstance()
                    cal.time = lockedAt.toDate()
                    cal.add(Calendar.HOUR, 24)
                    // Tự động mở khóa sau 24h
                    if (Timestamp.now().toDate().after(cal.time)) {
                        firestoreService.updateSecurityLog(email, mapOf("is_locked" to false, "failed_attempts" to 0))
                        return Result.success(false)
                    }
                }
            }
            Result.success(isLocked)
        } catch (e: Exception) {
            Result.success(false)
        }
    }
}
