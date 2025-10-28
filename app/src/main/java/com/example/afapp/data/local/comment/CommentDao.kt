package com.example.afapp.data.local.comment


import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Query("""
        SELECT 
            comments.*,
            users.name as authorName,
            users.email as authorEmail,
            users.profileImagePath as authorProfileImageUrl
        FROM comments
        INNER JOIN users ON comments.authorId = users.id
        WHERE comments.postId = :postId
        ORDER BY timestamp DESC
    """)
    fun observeByPost(postId: String): Flow<List<CommentWithAuthor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity)

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    suspend fun countByPost(postId: String): Int

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteById(commentId: String)
}