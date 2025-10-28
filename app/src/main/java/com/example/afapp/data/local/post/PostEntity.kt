package com.example.afapp.data.local.post

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.afapp.data.local.user.UserEntity
import com.example.afapp.domain.asfaltofashion.Category
import com.example.afapp.domain.asfaltofashion.Post

@Entity(
    tableName = "posts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("authorId")]
)
data class PostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: String,
    val authorId: Long,
    val publishedAt: Long,
    val imageUrl: String?
)

data class PostWithDetails(
    @Embedded val post: PostEntity,
    val authorName: String,
    val authorProfileImageUrl: String?,
    val likesCount: Int
)

fun PostWithDetails.toDomain() = Post(
    id = post.id,
    title = post.title,
    summary = post.summary,
    content = post.content,
    category = Category.valueOf(post.category), // <-- Usa el Enum importado
    authorName = authorName,
    authorProfileImageUrl = authorProfileImageUrl,
    publishedAt = post.publishedAt,
    likes = likesCount,
    imageUrl = post.imageUrl
)

fun Post.toEntity(authorId: Long) = PostEntity(
    id = id,
    title = title,
    summary = summary,
    content = content,
    category = category.name,
    authorId = authorId,
    publishedAt = publishedAt,
    imageUrl = imageUrl
)