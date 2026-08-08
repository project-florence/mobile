package com.florence.app.presentation.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.Contributor
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val repo: MiscRepository,
) : ViewModel() {

    data class AboutUiState(
        val loading: Boolean = true,
        val content: String? = null,
        val contributors: List<Contributor> = emptyList(),
        val error: Boolean = false,
    )

    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState: StateFlow<AboutUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = false) }
            val lang = if (Locale.getDefault().language == "en") "en" else "tr"
            val about = repo.about(lang).getOrNull()
            val contributors = repo.contributors().getOrNull() ?: emptyList()
            _uiState.update {
                it.copy(
                    loading = false,
                    content = about?.content,
                    contributors = contributors,
                    error = about == null,
                )
            }
        }
    }
}
