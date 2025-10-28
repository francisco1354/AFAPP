package com.example.afapp.utils

import android.content.Context
import com.example.afapp.data.local.database.AppDatabase
import com.example.afapp.data.repository.CommentRepository
import com.example.afapp.data.repository.LikeRepository
import com.example.afapp.data.repository.PostRepository
import com.example.afapp.data.repository.UserRepository

object ServiceLocator {

    @Volatile
    private var db: AppDatabase? = null

    private fun getDb(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = AppDatabase.getInstance(context)
            db = instance
            instance
        }
    }

    fun provideUserRepository(context: Context): UserRepository {
        val dao = getDb(context).userDao()
        return UserRepository(dao)
    }

    fun providePostRepository(context: Context): PostRepository {
        val db = getDb(context)
        return PostRepository(db.postDao(), db.userDao())
    }

    fun provideCommentRepository(context: Context): CommentRepository {
        val dao = getDb(context).commentDao()
        return CommentRepository(dao)
    }

    fun provideLikeRepository(context: Context): LikeRepository {
        val dao = getDb(context).likeDao()
        return LikeRepository(dao)
    }
}