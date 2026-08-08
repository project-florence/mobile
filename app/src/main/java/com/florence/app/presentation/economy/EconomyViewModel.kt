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
        val gold: Map<String, CurrencyQuote> = emptyMap(),
        val silver: CurrencyQuote? = null,
        val platinum: CurrencyQuote? = null,
        val palladium: CurrencyQuote? = null,
    )

    private val _uiState = MutableStateFlow(EconomyUiState())
    val uiState: StateFlow<EconomyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val currency = market.currency()
            val gold = market.goldPrices()
            val silver = market.silverPrice()
            val platinum = market.gramPlatinumPrice()
            val palladium = market.gramPalladiumPrice()
            _uiState.update {
                it.copy(
                    loading = false,
                    currency = currency.getOrNull() ?: emptyMap(),
                    gold = gold.getOrNull() ?: emptyMap(),
                    silver = silver.getOrNull()?.values?.firstOrNull(),
                    platinum = platinum.getOrNull()?.values?.firstOrNull(),
                    palladium = palladium.getOrNull()?.values?.firstOrNull(),
                )
            }
        }
    }
}
