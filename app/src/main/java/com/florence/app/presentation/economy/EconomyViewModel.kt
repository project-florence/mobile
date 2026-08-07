package com.florence.app.presentation.economy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.model.CurrencyQuote
import com.florence.app.data.repository.MarketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EconomyViewModel @Inject constructor(
    private val market: MarketRepository,
) : ViewModel() {

    data class EconomyUiState(
        val loading: Boolean = true,
        val currency: Map<String, CurrencyQuote> = emptyMap(),
        val currencyEmpty: Boolean = false,
    )

    private val _uiState = MutableStateFlow(EconomyUiState())
    val uiState: StateFlow<EconomyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val result = market.currency()
            _uiState.update {
                it.copy(
                    loading = false,
                    currency = result.getOrNull() ?: emptyMap(),
                    currencyEmpty = result.getOrNull()?.isEmpty() != false,
                )
            }
        }
    }
}
