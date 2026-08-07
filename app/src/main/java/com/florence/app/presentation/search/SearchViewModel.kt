package com.florence.app.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.CompanySearchResult
import com.florence.app.data.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val loading: Boolean = false,
    val searched: Boolean = false,
    val results: List<CompanySearchResult> = emptyList(),
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: MarketRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _query.debounce(300).collectLatest { q ->
                if (q.isBlank()) {
                    _uiState.value = SearchUiState()
                    return@collectLatest
                }
                _uiState.update { it.copy(query = q, loading = true, searched = true) }
                val result = repo.search(q)
                _uiState.update {
                    it.copy(loading = false, results = result.getOrNull() ?: emptyList())
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
        // Alan değeri anında yansımalı (kontrollü bileşen); debounce aramayı yönetir.
        _uiState.update { it.copy(query = value) }
    }
}
