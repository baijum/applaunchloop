package com.baijum.applaunchloop.ui.screen.tester

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.baijum.applaunchloop.data.repository.CampaignRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TesterUiState(
    val campaignId: String = "",
    val targetPackageName: String = "",
    val streakCount: Int = 0,
    val isComplete: Boolean = false,
    val hasActiveCampaign: Boolean = false
)

class TesterViewModel(
    private val campaignRepository: CampaignRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TesterUiState())
    val uiState: StateFlow<TesterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                campaignRepository.activeCampaignId,
                campaignRepository.targetPackageNames,
                campaignRepository.currentStreakCount
            ) { campaignId, packages, streak ->
                val pkg = packages.firstOrNull() ?: ""
                TesterUiState(
                    campaignId = campaignId,
                    targetPackageName = pkg,
                    streakCount = streak,
                    isComplete = streak >= 14,
                    hasActiveCampaign = campaignId.isNotBlank()
                )
            }.collect { _uiState.value = it }
        }
    }

    class Factory(
        private val campaignRepository: CampaignRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TesterViewModel(campaignRepository) as T
        }
    }
}
