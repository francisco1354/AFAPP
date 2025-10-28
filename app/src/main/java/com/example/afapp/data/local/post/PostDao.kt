package com.example.afapp.data.local.post

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.afapp.data.local.post.PostWithDetails
import com.example.afapp.data.local.post.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    // Para PostRepo.getAll()
    @Query("""
        SELECT 
            p.*, 
            u.name as authorName, 
            u.profileImagePath as authorProfileImageUrl,
            (SELECT COUNT(*) FROM likes WHERE likes.postId = p.id) as likesCount
        FROM posts as p
        INNER JOIN users as u ON p.authorId = u.id
        ORDER BY p.publishedAt DESC
    """)
    fun observeAll(): Flow<List<PostWithDetails>>

    // Para PostRepo.getByCategory(category: String)
    @Query("""
        SELECT 
            p.*, 
            u.name as authorName, 
            u.profileImagePath as authorProfileImageUrl,
            (SELECT COUNT(*) FROM likes WHERE likes.postId = p.id) as likesCount
        FROM posts as p
        INNER JOIN users as u ON p.authorId = u.id
        WHERE p.category = :category
        ORDER BY p.publishedAt DESC
    """)
    fun observeByCategory(category: String): Flow<List<PostWithDetails>>

    // Para PostRepo.getByAuthorEmail(email: String)
    @Query("""
        SELECT 
            p.*, 
            u.name as authorName, 
            u.profileImagePath as authorProfileImageUrl,
            (SELECT COUNT(*) FROM likes WHERE likes.postId = p.id) as likesCount
        FROM posts as p
        INNER JOIN users as u ON p.authorId = u.id
        WHERE u.email = :email
        ORDER BY p.publishedAt DESC
    """)
    fun observeByAuthorEmail(email: String): Flow<List<PostWithDetails>>

    // Para PostRepo.search(query: String)
    @Query("""
        SELECT 
            p.*, 
            u.name as authorName, 
            u.profileImagePath as authorProfileImageUrl,
            (SELECT COUNT(*) FROM likes WHERE likes.postId = p.id) as likesCount
        FROM posts as p
        INNER JOIN users as u ON p.authorId = u.id
        WHERE p.title LIKE '%' || :query || '%' OR p.summary LIKE '%' || :query || '%'
        ORDER BY p.publishedAt DESC
    """)
    fun searchPosts(query: String): Flow<List<PostWithDetails>>

    // Para PostRepo.get(id: String)
    @Query("""
        SELECT 
            p.*, 
            u.name as authorName, 
            u.profileImagePath as authorProfileImageUrl,
            (SELECT COUNT(*) FROM likes WHERE likes.postId = p.id) as likesCount
        FROM posts as p
        INNER JOIN users as u ON p.authorId = u.id
        WHERE p.id = :id
        LIMIT 1
    """)
    suspend fun getById(id: String): PostWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deleteById(postId: String)
}