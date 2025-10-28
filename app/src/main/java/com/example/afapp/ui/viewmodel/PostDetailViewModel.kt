package com.example.afapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afapp.data.local.comment.Comment
import com.example.afapp.data.local.like.Like
import com.example.afapp.data.repository.CommentRepository
import com.example.afapp.data.repository.LikeRepository
import com.example.afapp.data.repository.PostRepository
import com.example.afapp.data.repository.UserRepository
import com.example.afapp.domain.asfaltofashion.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostDetailUiState(
    val isLoadingPost: Boolean = true,
    val isLoadingComments: Boolean = true,
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val commentText: String = "",
    val likes: List<Like> = emptyList(),
    val hasUserLiked: Boolean = false,
    val isCurrentUserAdmin: Boolean = false
)

class PostDetailViewModel(
    private val postId: String,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(PostDetailUiState())
    val ui: StateFlow<PostDetailUiState> = _ui.asStateFlow()

    fun init(userEmail: String) {
        viewModelScope.launch {
            val user = userRepository.findByEmail(userEmail)
            _ui.update { it.copy(isCurrentUserAdmin = user?.isAdmin == true) }
        }
        loadPost()
        loadComments()
        observeLikes(userEmail)
    }

    private fun loadPost() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoadingPost = true) }
            val post = postRepository.get(postId)
            _ui.update { it.copy(post = post, isLoadingPost = false) }
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            commentRepository.observeByPost(postId).collect { comments ->
                _ui.update { it.copy(comments = comments, isLoadingComments = false) }
            }
        }
    }

    private fun observeLikes(userEmail: String) {
        viewModelScope.launch {
            likeRepository.getLikesForPost(postId).collect { likes ->
                val hasLiked = likes.any { it.userEmail == userEmail }
                _ui.update { it.copy(likes = likes, hasUserLiked = hasLiked) }
            }
        }
    }

    fun onCommentTextChange(text: String) {
        _ui.update { it.copy(commentText = text) }
    }

    fun addComment(authorId: Long) {
        viewModelScope.launch {
            val content = _ui.value.commentText
            if (content.isNotBlank()) {
                commentRepository.addComment(postId, authorId, content)
                _ui.update { it.copy(commentText = "") }
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            commentRepository.deleteComment(commentId)
        }
    }

    fun toggleLike(userEmail: String) {
        viewModelScope.launch {
            likeRepository.toggleLike(postId, userEmail)
        }
    }


    fun deletePost(onPostDeleted: () -> Unit) {
        viewModelScope.launch {
            postRepository.delete(postId)
            onPostDeleted()
        }
    }
}