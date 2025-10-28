package com.example.afapp.data.repository

import com.example.afapp.data.local.like.LikeDao
import com.example.afapp.data.local.like.LikeEntity
import com.example.afapp.data.local.like.toDomain
import kotlinx.coroutines.flow.map

class LikeRepository(private val likeDao: LikeDao) {

    fun getLikesForPost(postId: String) = likeDao.observeByPost(postId).map { list ->
        list.map { it.toDomain() }
    }

    suspend fun toggleLike(postId: String, userEmail: String) {
        val existingLike = likeDao.getUserLike(postId, userEmail)
        if (existingLike != null) {
            likeDao.delete(existingLike)
        } else {
            val like = LikeEntity(postId = postId, userEmail = userEmail)
            likeDao.insert(like)
        }
    }
}