package com.example.afapp.data.local.like

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LikeDao {

    @Query("""
        SELECT 
            likes.*,
            users.name as userName,
            users.profileImagePath as userProfileImageUrl
        FROM likes
        INNER JOIN users ON likes.userEmail = users.email
        WHERE likes.postId = :postId
        ORDER BY timestamp DESC
    """)
    fun observeByPost(postId: String): Flow<List<LikeWithUser>>

    @Query("SELECT * FROM likes WHERE postId = :postId AND userEmail = :userEmail LIMIT 1")
    suspend fun getUserLike(postId: String, userEmail: String): LikeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(like: LikeEntity)

    @Delete
    suspend fun delete(like: LikeEntity)
}