package com.example.afapp.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.afapp.data.local.comment.Comment
import com.example.afapp.data.local.like.Like
import com.example.afapp.data.local.storage.UserPreferences
import com.example.afapp.ui.viewmodel.PostDetailViewModel
import com.example.afapp.utils.ImageUtils
import com.example.afapp.utils.ServiceLocator
import java.text.SimpleDateFormat
import java.util.*
import com.example.afapp.domain.asfaltofashion.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val user by userPrefs.isLoggedIn.collectAsState(initial = false)
    val lastEmail by userPrefs.lastEmail.collectAsState(initial = null)

    val vm: PostDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val postRepo = ServiceLocator.providePostRepository(context)
                val commentRepo = ServiceLocator.provideCommentRepository(context)
                val likeRepo = ServiceLocator.provideLikeRepository(context)
                val userRepo = ServiceLocator.provideUserRepository(context)
                @Suppress("UNCHECKED_CAST")
                return PostDetailViewModel(postId, postRepo, commentRepo, likeRepo, userRepo) as T
            }
        }
    )

    val ui by vm.ui.collectAsStateWithLifecycle()

    LaunchedEffect(lastEmail) {
        lastEmail?.let { vm.init(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ui.post?.title ?: "...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    if (ui.isCurrentUserAdmin) {
                        IconButton(onClick = { vm.deletePost(onBack) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar post", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "¡Mira este post de Asfalto Fashion!\n\n${ui.post?.title}\n\n${ui.post?.summary}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = MaterialTheme.colorScheme.onPrimary)
                    }

                    if(user) {
                        IconToggleButton(checked = ui.hasUserLiked, onCheckedChange = { lastEmail?.let { vm.toggleLike(it) } }) {
                            Icon(
                                imageVector = if (ui.hasUserLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Me gusta",
                                tint = if (ui.hasUserLiked) Color.Red else MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                if(ui.isLoadingPost) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                } else {
                    ui.post?.let {
                        PostContent(post = it)
                    }
                }
            }

            item {
                LikesSection(likes = ui.likes)
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Comentarios (${ui.comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            if (ui.isLoadingComments) {
                item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
            } else {
                items(ui.comments) { comment ->
                    val currentUserEmail = lastEmail
                    CommentCard(
                        comment = comment,
                        canDelete = (currentUserEmail == comment.authorEmail) || ui.isCurrentUserAdmin,
                        onDelete = { vm.deleteComment(comment.id) }
                    )
                }
            }

            item {
                if(user) {
                    val userRepo = ServiceLocator.provideUserRepository(context)
                    var userId by remember { mutableLongStateOf(0L) }
                    LaunchedEffect(lastEmail) {
                        lastEmail?.let {
                            userId = userRepo.findByEmail(it)?.id ?: 0L
                        }
                    }
                    CommentInput(
                        commentText = ui.commentText,
                        onCommentChange = { vm.onCommentTextChange(it) },
                        onSend = {
                            vm.addComment(userId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PostContent(post: Post) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        if (post.imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(ImageUtils.getFileUri(post.imageUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = "Imagen del post",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
        }

        Text(post.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(post.summary, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Text(post.content, style = MaterialTheme.typography.bodyLarge)
    }
}


@Composable
private fun LikesSection(likes: List<Like>) {
    if (likes.isNotEmpty()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text("Le ha gustado a:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(likes) { like ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (like.userProfileImageUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(ImageUtils.getFileUri(like.userProfileImageUrl))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(like.userName.firstOrNull()?.toString() ?: "?", color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                        Text(like.userName, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentCard(comment: Comment, canDelete: Boolean, onDelete: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (comment.authorProfileImageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(ImageUtils.getFileUri(comment.authorProfileImageUrl))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de perfil del autor",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        comment.authorName.firstOrNull()?.toString()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(comment.authorName, fontWeight = FontWeight.Bold)
                Text(
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(comment.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(comment.content)
            }

            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar comentario", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentInput(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Escribe un comentario...") },
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = onSend, enabled = commentText.isNotBlank()) {
            Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
