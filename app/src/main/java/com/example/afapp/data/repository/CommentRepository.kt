package com.example.afapp.data.repository

import com.example.afapp.data.local.comment.CommentDao
import com.example.afapp.data.local.comment.CommentEntity
import com.example.afapp.data.local.comment.toDomain
import kotlinx.coroutines.flow.map
import java.util.UUID

class CommentRepository(
    private val commentDao: CommentDao
) {
    fun observeByPost(postId: String) = commentDao.observeByPost(postId).map { list ->
        list.map { it.toDomain() } }

    suspend fun addComment(postId: String, authorId: Long, content: String) {
        val comment = CommentEntity(
            id = UUID.randomUUID().toString(),
            postId = postId,
            authorId = authorId,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        commentDao.insert(comment)
    }

    suspend fun deleteComment(commentId: String) {
        commentDao.deleteById(commentId)
    }
}
