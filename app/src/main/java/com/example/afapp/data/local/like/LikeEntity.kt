package com.example.afapp.data.local.like


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "likes",
    primaryKeys = ["postId", "userEmail"],
    indices = [Index(value = ["postId"]), Index(value = ["userEmail"])]
)
data class LikeEntity(
    val postId: String,
    val userEmail: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class LikeWithUser(
    @Embedded val like: LikeEntity,
    val userName: String,
    val userProfileImageUrl: String?
)

data class Like(
    val postId: String,
    val userEmail: String,
    val userName: String,
    val userProfileImageUrl: String?,
    val timestamp: Long
)

fun LikeWithUser.toDomain() = Like(
    postId = like.postId,
    userEmail = like.userEmail,
    userName = userName,
    userProfileImageUrl = userProfileImageUrl,
    timestamp = like.timestamp
)