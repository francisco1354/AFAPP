package com.example.afapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.afapp.data.local.storage.UserPreferences
import com.example.afapp.data.local.user.UserEntity
import com.example.afapp.navigation.Route
import com.example.afapp.ui.viewmodel.DrawerViewModel
import com.example.afapp.ui.viewmodel.DrawerViewModelFactory
import com.example.afapp.utils.ImageUtils

@Composable
fun AppDrawer(
    currentRoute: String?,
    isLoggedIn: Boolean,
    user: UserEntity?,
    onHome: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onProfile: () -> Unit,
    onCreatePost: () -> Unit,
    onMyPosts: () -> Unit,
    onLogout: () -> Unit,
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val vm: DrawerViewModel = viewModel(factory = DrawerViewModelFactory(userPrefs))
    val theme by vm.theme.collectAsState()

    ModalDrawerSheet {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Encabezado
            if (isLoggedIn && user != null) {
                DrawerHeader(user)
            } else {
                DrawerHeader(null)
            }

            // Opciones de navegación
            NavigationDrawerItem(
                label = { Text("Inicio") },
                selected = currentRoute == Route.HomeBase.path,
                onClick = onHome,
                icon = { Icon(Icons.Default.Home, null) }
            )
            if (isLoggedIn) {
                NavigationDrawerItem(
                    label = { Text("Mi Perfil") },
                    selected = currentRoute == Route.Profile.path,
                    onClick = onProfile,
                    icon = { Icon(Icons.Default.Person, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Crear Post") },
                    selected = currentRoute == Route.Create.path,
                    onClick = onCreatePost,
                    icon = { Icon(Icons.Default.AddCircle, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Mis Publicaciones") },
                    selected = currentRoute?.startsWith(Route.HomeFiltered.path.substringBefore("/")) ?: false,
                    onClick = onMyPosts,
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, null) }
                )
            } else {
                NavigationDrawerItem(
                    label = { Text("Login") },
                    selected = currentRoute == Route.Login.path,
                    onClick = onLogin,
                    icon = { Icon(Icons.AutoMirrored.Filled.Login, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Registro") },
                    selected = currentRoute == Route.Register.path,
                    onClick = onRegister,
                    icon = { Icon(Icons.Default.PersonAdd, null) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Selector de tema
            ThemeSelector(selectedTheme = theme, onThemeChange = vm::saveTheme)

            // Logout
            if (isLoggedIn) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = onLogout,
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null) }
                )
            }
        }
    }
}

@Composable
private fun DrawerHeader(user: UserEntity?) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user?.profileImagePath != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(ImageUtils.getFileUri(user.profileImagePath))
                    .crossfade(true)
                    .build(),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user?.name?.firstOrNull()?.toString() ?: "?",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                user?.name ?: "Invitado",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            if (user != null) {
                Text(user.email, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(selectedTheme: String, onThemeChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("Tema", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = selectedTheme == "light",
                onClick = { onThemeChange("light") },
                shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
            ) {
                Text("Claro")
            }
            SegmentedButton(
                selected = selectedTheme == "system",
                onClick = { onThemeChange("system") },
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Sistema")
            }
            SegmentedButton(
                selected = selectedTheme == "dark",
                onClick = { onThemeChange("dark") },
                shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
            ) {
                Text("Oscuro")
            }
        }
    }
}
