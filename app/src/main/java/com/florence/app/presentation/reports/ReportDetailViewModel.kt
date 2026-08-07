package com.florence.app.presentation.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.ReportDetail
import com.florence.app.data.repository.MiscRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MiscRepository,
) : ViewModel() {

    private val reportId: Int = savedStateHandle["reportId"] ?: 0

    data class ReportDetailUiState(
        val loading: Boolean = true,
        val error: String? = null,
        val report: ReportDetail? = null,
    )

    private val _uiState = MutableStateFlow(ReportDetailUiState())
    val uiState: StateFlow<ReportDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = repo.reportDetail(reportId)
            _uiState.update {
                it.copy(
                    loading = false,
                    report = result.getOrNull(),
                    error = if (result.isFailure) "Rapor yüklenemedi" else null,
                )
            }
        }
    }
}
