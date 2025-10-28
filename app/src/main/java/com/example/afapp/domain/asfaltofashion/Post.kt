package com.example.afapp.domain.asfaltofashion

data class Post(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: Category,
    val authorName: String,
    val authorProfileImageUrl: String?,
    val publishedAt: Long,
    val likes: Int,
    val imageUrl: String?
)