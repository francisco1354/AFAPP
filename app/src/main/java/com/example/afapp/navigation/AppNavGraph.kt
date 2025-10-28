package com.example.afapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.afapp.ui.viewmodel.SessionViewModel
import kotlinx.coroutines.launch

import com.example.afapp.data.local.storage.UserPreferences
import com.example.afapp.ui.components.AppDrawer
import com.example.afapp.ui.components.AppTopBar
import com.example.afapp.ui.screens.HomeScreen
import com.example.afapp.ui.screens.LoginScreenVm
import com.example.afapp.ui.screens.RegisterScreenVm
import com.example.afapp.ui.viewmodel.AuthViewModel
import com.example.afapp.utils.ServiceLocator
import com.example.afapp.ui.viewmodel.PostViewModelFactory
import com.example.afapp.ui.viewmodel.PostViewModel
import com.example.afapp.ui.screens.CreatePostScreen
import com.example.afapp.ui.screens.PostDetailScreen
import com.example.afapp.ui.screens.ProfileScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    sessionViewModel: SessionViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val postRepo = remember { ServiceLocator.providePostRepository(context) }

    val userPrefs = remember { UserPreferences(context) }
    val isLoggedIn by userPrefs.isLoggedIn.collectAsStateWithLifecycle(false)
    val lastEmail by userPrefs.lastEmail.collectAsStateWithLifecycle(null)

    val currentUser by sessionViewModel.currentUser.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn, lastEmail) {
        val email = lastEmail
        if (isLoggedIn && !email.isNullOrBlank()) {
            sessionViewModel.loadUser(email)
        } else {
            sessionViewModel.clearSession()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun safeNavigate(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
        }
    }

    val goHome: () -> Unit = {
        scope.launch { drawerState.close() }
        navController.navigate(Route.HomeBase.path) {
            popUpTo(Route.Login.path) { inclusive = true } // Pop up to login to clear the auth flow
            launchSingleTop = true
        }
    }
    val goLogin: () -> Unit = {
        scope.launch { drawerState.close() }
        safeNavigate(Route.Login.path)
    }
    val goRegister: () -> Unit = {
        scope.launch { drawerState.close() }
        safeNavigate(Route.Register.path)
    }
    val goCreate: () -> Unit = {
        scope.launch { drawerState.close() }
        safeNavigate(Route.Create.path)
    }
    val goDetail: (String) -> Unit = { id ->
        safeNavigate(Route.Detail.path(id))
    }
    val goProfile: () -> Unit = {
        scope.launch { drawerState.close() }
        safeNavigate(Route.Profile.path)
    }

    val goMyPosts: (String) -> Unit = { email ->
        scope.launch { drawerState.close() }
        navController.navigate(Route.HomeFiltered.path(email)) {
            popUpTo(Route.HomeBase.path) { inclusive = true }
            launchSingleTop = true
        }
    }

    val handleLogout: () -> Unit = {
        scope.launch {
            drawerState.close()
            userPrefs.setLoggedIn(false)
            sessionViewModel.clearSession()
            navController.navigate(Route.Login.path) {
                popUpTo(Route.HomeBase.path) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val startDestination = if (isLoggedIn) Route.HomeBase.path else Route.Login.path

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                isLoggedIn = isLoggedIn,
                user = currentUser,
                onHome = goHome,
                onLogin = goLogin,
                onRegister = goRegister,
                onProfile = goProfile,
                onCreatePost = goCreate,
                onMyPosts = {
                    currentUser?.email?.let { email ->
                        goMyPosts(email)
                    }
                },
                onLogout = handleLogout
            )
        }
    ) {
        Scaffold(
            topBar = {
                // Solo mostramos la TopBar si no estamos en Login o Register
                if (currentRoute != Route.Login.path && currentRoute != Route.Register.path) {
                    AppTopBar(
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {

                composable(route = Route.HomeBase.path) {
                    val postViewModel: PostViewModel = viewModel(
                        factory = PostViewModelFactory(
                            repo = postRepo,
                            filterEmail = null
                        )
                    )
                    HomeScreen(
                        vm = postViewModel,
                        onGoLogin = goLogin,
                        onGoRegister = goRegister,
                        onCreate = goCreate,
                        onOpenDetail = goDetail
                    )
                }

                composable(
                    route = Route.HomeFiltered.path,
                    arguments = listOf(
                        navArgument(Route.FILTER_EMAIL_ARG) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val filterEmail = backStackEntry.arguments?.getString(Route.FILTER_EMAIL_ARG)
                    val postViewModel: PostViewModel = viewModel(
                        factory = PostViewModelFactory(
                            repo = postRepo,
                            filterEmail = filterEmail
                        )
                    )
                    HomeScreen(
                        vm = postViewModel,
                        onGoLogin = goLogin,
                        onGoRegister = goRegister,
                        onCreate = goCreate,
                        onOpenDetail = goDetail
                    )
                }

                composable(Route.Login.path) {
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Register.path) {
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }
                composable(Route.Create.path) {
                    CreatePostScreen(onBack = { navController.popBackStack() })
                }

                composable(Route.Detail.path) { backStack ->
                    val id = backStack.arguments?.getString("postId") ?: ""
                    PostDetailScreen(
                        postId = id,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Route.Profile.path) {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onLogout = handleLogout,
                        sessionViewModel = sessionViewModel
                    )
                }
            }
        }
    }
}
