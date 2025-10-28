package com.example.afapp.data.repository

import com.example.afapp.data.local.post.PostDao
import com.example.afapp.data.local.post.toDomain
import com.example.afapp.data.local.post.toEntity
import com.example.afapp.data.local.user.UserDao
import com.example.afapp.domain.asfaltofashion.Post
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class PostRepository(
    private val postDao: PostDao,
    private val userDao: UserDao
) {

    fun getAll() = postDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun getByCategory(category: String) = postDao.observeByCategory(category).map { list -> list.map { it.toDomain() } }

    fun getByAuthorEmail(email: String) = postDao.observeByAuthorEmail(email).map { list -> list.map { it.toDomain() } }

    fun search(query: String): Flow<List<Post>> {
        return postDao.searchPosts(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun get(id: String): Post? {
        return postDao.getById(id)?.toDomain()
    }

    suspend fun create(post: Post, authorEmail: String) {
        val user = userDao.findByEmail(authorEmail) ?: throw Exception("Usuario no encontrado")
        val entity = post.copy(id = UUID.randomUUID().toString()).toEntity(user.id)
        postDao.insert(entity)
    }

    suspend fun delete(postId: String) {
        postDao.deleteById(postId)
    }
}