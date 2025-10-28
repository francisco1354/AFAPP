package com.example.afapp.data.repository

import com.example.afapp.data.local.user.UserDao
import com.example.afapp.data.local.user.UserEntity
import com.example.afapp.utils.PasswordUtils

class UserRepository(
    private val userDao: UserDao
) {

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = userDao.findByEmail(email)
        return if (user != null && PasswordUtils.verifyPassword(password, user.password)) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Credenciales inválidas"))
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<Long> {
        val hashedPassword = PasswordUtils.hashPassword(password)

        val id = userDao.insert(
            UserEntity(
                name = name,
                email = email,
                phone = phone,
                password = hashedPassword
            )
        )
        return if (id == -1L) {
            Result.failure(IllegalStateException("El correo ya está registrado"))
        } else {
            Result.success(id)
        }
    }

    suspend fun findByEmail(email: String): UserEntity? =
        userDao.findByEmail(email)

    suspend fun update(user: UserEntity) =
        userDao.update(user)
}