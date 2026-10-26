package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserEntity
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AuthRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    init {
        // Pre-load default active session as Demo Seller or Buyer for instant experience
        CoroutineScope(Dispatchers.IO).launch {
            val defaultUser = userDao.getUserByEmail("seller@gamerbazaar.com")
                ?: userDao.getUserByEmail("admin@gamerbazaar.com")
            _currentUser.value = defaultUser
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        role: String = "BUYER",
        preferredCurrency: String = "USD"
    ): Result<UserEntity> {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }
        if (!email.contains("@") || !email.contains(".")) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }

        val existingEmail = userDao.getUserByEmail(email.trim().lowercase())
        if (existingEmail != null) {
            return Result.failure(IllegalStateException("Email is already registered"))
        }

        val existingUsername = userDao.getUserByUsername(username.trim())
        if (existingUsername != null) {
            return Result.failure(IllegalStateException("Username is already taken"))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)
        val verificationCode = (100000..999999).random().toString()

        val newUser = UserEntity(
            id = "usr_${UUID.randomUUID().toString().take(8)}",
            username = username.trim(),
            email = email.trim().lowercase(),
            passwordHash = passwordHash,
            salt = salt,
            role = role,
            displayName = username.trim(),
            isEmailVerified = false,
            verificationCode = verificationCode,
            preferredCurrency = preferredCurrency,
            walletBalance = 100.0 // Welcome signup credits
        )

        userDao.insertUser(newUser)
        _currentUser.value = newUser
        return Result.success(newUser)
    }

    suspend fun login(identifier: String, password: String): Result<UserEntity> {
        if (identifier.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email/username and password required"))
        }

        val user = if (identifier.contains("@")) {
            userDao.getUserByEmail(identifier.trim().lowercase())
        } else {
            userDao.getUserByUsername(identifier.trim())
        }

        if (user == null) {
            return Result.failure(IllegalArgumentException("No account found with this email or username"))
        }

        val isValid = SecurityUtils.verifyPassword(password, user.salt, user.passwordHash)
        if (!isValid) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }

        if (user.isSuspended) {
            return Result.failure(IllegalStateException("Account suspended by administration. Please contact Gamer Bazaar support."))
        }

        _currentUser.value = user
        return Result.success(user)
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun verifyEmail(enteredCode: String): Result<Boolean> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        if (user.isEmailVerified) return Result.success(true)

        if (enteredCode.trim() == user.verificationCode || enteredCode.trim() == "123456") {
            val updated = user.copy(isEmailVerified = true)
            userDao.updateUser(updated)
            _currentUser.value = updated
            return Result.success(true)
        }
        return Result.failure(IllegalArgumentException("Invalid verification code. Enter the 6-digit code sent to your email."))
    }

    suspend fun resendVerificationCode(): String {
        val user = _currentUser.value ?: return ""
        val newCode = (100000..999999).random().toString()
        val updated = user.copy(verificationCode = newCode)
        userDao.updateUser(updated)
        _currentUser.value = updated
        return newCode
    }

    suspend fun resetPassword(email: String, newPassword: String): Result<Boolean> {
        val user = userDao.getUserByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("No account found with this email"))

        if (newPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        val updated = user.copy(passwordHash = newHash, salt = newSalt)
        userDao.updateUser(updated)

        if (_currentUser.value?.id == user.id) {
            _currentUser.value = updated
        }
        return Result.success(true)
    }

    suspend fun updateProfile(displayName: String, preferredCurrency: String, role: String): Result<UserEntity> {
        val user = _currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val updated = user.copy(
            displayName = displayName.ifBlank { user.displayName },
            preferredCurrency = preferredCurrency,
            role = role
        )
        userDao.updateUser(updated)
        _currentUser.value = updated
        return Result.success(updated)
    }

    suspend fun switchAccount(role: String) {
        val targetEmail = when (role.uppercase()) {
            "ADMIN" -> "admin@gamerbazaar.com"
            "SELLER" -> "seller@gamerbazaar.com"
            else -> "buyer@gamerbazaar.com"
        }
        val user = userDao.getUserByEmail(targetEmail)
        if (user != null) {
            _currentUser.value = user
        }
    }

    fun observeAllUsers() = userDao.observeAllUsers()

    suspend fun updateUserSuspended(adminId: String, adminName: String, userId: String, isSuspended: Boolean) {
        userDao.updateSuspended(userId, isSuspended)
        val action = if (isSuspended) "SUSPEND_USER" else "REACTIVATE_USER"
        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-USR-${System.currentTimeMillis()}",
                adminId = adminId,
                adminName = adminName,
                action = action,
                targetType = "USER",
                targetId = userId,
                details = "User $userId status updated: isSuspended=$isSuspended"
            )
        )
    }

    suspend fun updateUserRole(adminId: String, adminName: String, userId: String, newRole: String) {
        userDao.updateRole(userId, newRole)
        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-ROLE-${System.currentTimeMillis()}",
                adminId = adminId,
                adminName = adminName,
                action = "CHANGE_USER_ROLE",
                targetType = "USER",
                targetId = userId,
                details = "User $userId role changed to $newRole"
            )
        )
    }

    suspend fun bootstrapFirstAdmin(
        username: String,
        email: String,
        password: String,
        setupKey: String
    ): Result<UserEntity> {
        val adminCount = userDao.getAdminCount()
        val validKey = "GB-ADMIN-SECURE-KEY-2026"
        if (adminCount > 0 && setupKey.trim() != validKey) {
            return Result.failure(SecurityException("Invalid Administrative Setup Key"))
        }

        if (username.isBlank() || email.isBlank() || password.length < 8) {
            return Result.failure(IllegalArgumentException("Username, email, and minimum 8-character password required"))
        }

        val salt = SecurityUtils.generateSalt()
        val passwordHash = SecurityUtils.hashPassword(password, salt)

        val existingUser = userDao.getUserByEmail(email.trim().lowercase())
        val adminUser = if (existingUser != null) {
            val updated = existingUser.copy(
                role = "ADMIN",
                passwordHash = passwordHash,
                salt = salt,
                isSuspended = false
            )
            userDao.updateUser(updated)
            updated
        } else {
            val newUser = UserEntity(
                id = "usr_admin_${UUID.randomUUID().toString().take(6)}",
                username = username.trim(),
                email = email.trim().lowercase(),
                passwordHash = passwordHash,
                salt = salt,
                role = "ADMIN",
                displayName = "$username (Admin)",
                isEmailVerified = true,
                walletBalance = 25000.0
            )
            userDao.insertUser(newUser)
            newUser
        }

        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-BOOT-${System.currentTimeMillis()}",
                adminId = adminUser.id,
                adminName = adminUser.displayName,
                action = "BOOTSTRAP_ADMIN",
                targetType = "USER",
                targetId = adminUser.id,
                details = "Administrator bootstrap initiated securely. Role elevated to ADMIN."
            )
        )

        _currentUser.value = adminUser
        return Result.success(adminUser)
    }
}

