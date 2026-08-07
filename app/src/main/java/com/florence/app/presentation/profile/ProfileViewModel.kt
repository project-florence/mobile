package com.florence.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florence.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: AuthRepository,
) : ViewModel() {

    val session: StateFlow<Boolean> = repo.session

    fun logout() {
        viewModelScope.launch { repo.logout() }
    }
}
