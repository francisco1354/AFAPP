package com.example.afapp.data.local.comment

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.afapp.data.local.post.PostEntity
import com.example.afapp.data.local.user.UserEntity

// 1. MODELO DE DOMINIO (Mantenemos nombres)
data class Comment(
    val id: String,
    val postId: String,
    val authorName: String,
    val authorEmail: String,
    val authorProfileImageUrl: String?,
    val content: String,
    val timestamp: Long
)

// 2. ENTIDAD DE ROOM (Mantenemos nombres)
@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["id"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: Long,
    val content: String,
    val timestamp: Long
)

// 3. CLASE PARA EL JOIN (Mantenemos nombres)
data class CommentWithAuthor(
    @Embedded val comment: CommentEntity,
    val authorName: String,
    val authorEmail: String,
    val authorProfileImageUrl: String?
)

// 4. CONVERSOR A MODELO DE DOMINIO
fun CommentWithAuthor.toDomain(): Comment {
    return Comment(
        id = comment.id,
        postId = comment.postId,
        authorName = authorName,
        authorEmail = authorEmail,
        authorProfileImageUrl = authorProfileImageUrl,
        content = comment.content,
        timestamp = comment.timestamp
    )
}