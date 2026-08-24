package com.florence.app.data.repository

import com.florence.app.data.api.FlorenceApi
import com.florence.app.data.model.EstimateCostResponse
import com.florence.app.data.model.SimulationDailyCostResponse
import com.florence.app.data.model.SimulationDetailResponse
import com.florence.app.data.model.SimulationHistoryItem
import com.florence.app.data.model.SimulationResponse
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simülasyon uçları (Monte Carlo). simulate maliyetlidir — 402 "insufficient credit"
 * ApiErrorMapper üzerinden i18n mesajına dönüştürülür.
 */
@Singleton
class SimulationRepository @Inject constructor(private val api: FlorenceApi) {

    suspend fun dailyCost(): Result<SimulationDailyCostResponse> =
        runCatching { api.simulationDailyCost() }

    suspend fun estimateCost(ticker: String, days: Int): Result<EstimateCostResponse> =
        runCatching { api.estimateSimulationCost(ticker, days) }

    suspend fun history(limit: Int = 20, offset: Int = 0): Result<List<SimulationHistoryItem>> =
        runCatching { api.simulationHistory(limit, offset) }

    suspend fun detail(simId: Int): Result<SimulationDetailResponse> =
        runCatching { api.simulationDetail(simId) }

    suspend fun simulate(
        ticker: String,
        days: Int,
        bounds: String,
        target: String?,
    ): Result<SimulationResponse> =
        runCatching { api.simulate(ticker, days, bounds, target) }
}