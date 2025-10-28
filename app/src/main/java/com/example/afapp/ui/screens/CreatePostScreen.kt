package com.example.afapp.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.afapp.data.local.storage.UserPreferences
import com.example.afapp.domain.asfaltofashion.Category
import com.example.afapp.ui.viewmodel.CreatePostViewModel
import com.example.afapp.ui.viewmodel.CreatePostViewModelFactory
import com.example.afapp.utils.ServiceLocator

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val postRepository = ServiceLocator.providePostRepository(context)
    val userRepository = ServiceLocator.provideUserRepository(context)

    val vm: CreatePostViewModel = viewModel(
        factory = CreatePostViewModelFactory(postRepository)
    )

    val userPrefs = remember { UserPreferences(context) }
    val userEmail by userPrefs.lastEmail.collectAsStateWithLifecycle("")

    var authorName by remember { mutableStateOf("Invitado") }

    LaunchedEffect(userEmail) {
        if (!userEmail.isNullOrBlank()) {
            val user = userRepository.findByEmail(userEmail!!)
            authorName = user?.name ?: userEmail!!.substringBefore("@")
        }
    }

    val ui by vm.ui.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        vm.onImageSelected(uri)
    }

    LaunchedEffect(ui.success) {
        if (ui.success) {
            vm.reset()
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Post") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, "Cancelar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padd ->
        Column(
            modifier = Modifier
                .padding(padd)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Publicando como: $authorName",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = ui.title,
                onValueChange = vm::onTitleChange,
                label = { Text("Título") },
                placeholder = { Text("Escribe un título llamativo...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = ui.summary,
                onValueChange = vm::onSummaryChange,
                label = { Text("Resumen") },
                placeholder = { Text("Breve descripción del post...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = ui.content,
                onValueChange = vm::onContentChange,
                label = { Text("Contenido") },
                placeholder = { Text("Escribe el contenido completo...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8
            )
            Spacer(Modifier.height(16.dp))

            Text(
                "Categoría",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Category.entries.forEach { c -> // <-- Usa el Enum de asfaltofashion
                    FilterChip(
                        selected = c == ui.category,
                        onClick = { vm.onCategoryChange(c) },
                        label = { Text(c.name) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Imagen (opcional)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))

            val imageUri = ui.imageUri
            if (imageUri != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUri.toUri())
                                .crossfade(true)
                                .build(),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { vm.onImageSelected(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.error,
                                    RoundedCornerShape(50)
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Quitar imagen",
                                tint = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Seleccionar imagen")
                }
            }

            if (ui.errorMsg != null) {
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        ui.errorMsg ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    enabled = !ui.isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        vm.create(
                            authorName = authorName,
                            authorEmail = userEmail ?: "",
                            context = context
                        )
                    },
                    enabled = !ui.isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (ui.isSubmitting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Publicando...")
                    } else {
                        Text("Publicar")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
