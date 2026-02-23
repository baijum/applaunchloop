package com.baijum.applaunchloop.ui.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.baijum.applaunchloop.data.repository.CampaignRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 3,
    val campaignId: String = "",
    val googleGroupEmail: String = "",
    val packageName: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class OnboardingViewModel(
    campaignId: String,
    private val campaignRepository: CampaignRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState(campaignId = campaignId))
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val firestore = Firebase.firestore

    init {
        loadCampaign(campaignId)
    }

    private fun loadCampaign(campaignId: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("campaigns")
                    .document(campaignId)
                    .get()
                    .await()

                if (doc.exists()) {
                    _uiState.value = _uiState.value.copy(
                        googleGroupEmail = doc.getString("googleGroupEmail") ?: "",
                        packageName = doc.getString("packageName") ?: "",
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Campaign not found."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load campaign."
                )
            }
        }
    }

    fun advanceStep() {
        val current = _uiState.value.currentStep
        if (current < _uiState.value.totalSteps - 1) {
            _uiState.value = _uiState.value.copy(currentStep = current + 1)
        }
    }

    fun saveAndFinish(onComplete: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            campaignRepository.setActiveCampaignId(state.campaignId)
            campaignRepository.setTargetPackageNames(setOf(state.packageName))
            campaignRepository.resetStreak()
            campaignRepository.updateLastRunTimestamp(0L)
            onComplete()
        }
    }

    class Factory(
        private val campaignId: String,
        private val campaignRepository: CampaignRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(campaignId, campaignRepository) as T
        }
    }
}
