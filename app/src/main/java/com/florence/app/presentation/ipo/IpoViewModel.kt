package com.florence.app.presentation.ipo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.IpoItem
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IpoViewModel @Inject constructor(
    private val repo: MiscRepository,
) : ViewModel() {

    data class IpoUiState(
        val loading: Boolean = true,
        val ipos: List<IpoItem> = emptyList(),
    )

    private val _uiState = MutableStateFlow(IpoUiState())
    val uiState: StateFlow<IpoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = repo.ipos()
            _uiState.update {
                it.copy(loading = false, ipos = result.getOrNull() ?: emptyList())
            }
        }
    }
}
