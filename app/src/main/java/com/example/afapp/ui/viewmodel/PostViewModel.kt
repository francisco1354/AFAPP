package com.example.afapp.ui.viewmodel // ⬅️ CORREGIDO

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afapp.data.repository.PostRepository // ⬅️ CORREGIDO
import com.example.afapp.domain.asfaltofashion.Category // ⬅️ CORREGIDO
import com.example.afapp.domain.asfaltofashion.Post // ⬅️ CORREGIDO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ⚠️ CORREGÍ EL NOMBRE DE LA CLASE
class PostViewModel(
    private val repo: PostRepository,
    private val filterEmail: String? = null
) : ViewModel() {

    private val _category = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _category.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _ui = MutableStateFlow(PostUiState())
    val ui: StateFlow<PostUiState> = _ui.asStateFlow()

    init {
        loadPosts()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadPosts() {
        viewModelScope.launch {
            combine(_category, _searchQuery) { cat, search ->
                Pair(cat, search)
            }.flatMapLatest { (cat, search) ->
                _ui.update { it.copy(isLoading = true) }
                when {
                    search.isNotBlank() -> repo.search(search)
                    filterEmail != null -> repo.getByAuthorEmail(filterEmail)
                    cat == null -> repo.getAll()
                    else -> repo.getByCategory(cat.name)
                }
            }.collect { posts ->
                _ui.update { it.copy(isLoading = false, posts = posts) }
            }
        }
    }

    fun onCategorySelect(c: Category?) {
        _category.value = c
        _searchQuery.value = "" // Limpia la búsqueda al seleccionar categoría
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _category.value = null // Limpia la categoría al buscar
        }
    }

    @Suppress("unused")
    suspend fun getById(id: String): Post? = repo.get(id)
}

// Data class para el estado de la UI
data class PostUiState(
    val isLoading: Boolean = true,
    val posts: List<Post> = emptyList()
)