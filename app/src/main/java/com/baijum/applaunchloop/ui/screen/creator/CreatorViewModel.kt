package com.baijum.applaunchloop.ui.screen.creator

import android.provider.Settings
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
import java.util.UUID

data class CampaignItem(
    val id: String,
    val packageName: String = "",
    val googleGroupEmail: String = ""
)

data class CreatorUiState(
    val googleGroupEmail: String = "",
    val packageName: String = "",
    val isLoading: Boolean = false,
    val generatedCampaignId: String? = null,
    val errorMessage: String? = null,
    val myCampaigns: List<CampaignItem> = emptyList(),
    val isLoadingCampaigns: Boolean = true
)

class CreatorViewModel(
    private val campaignRepository: CampaignRepository,
    private val deviceId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatorUiState())
    val uiState: StateFlow<CreatorUiState> = _uiState.asStateFlow()

    private val firestore = Firebase.firestore

    init {
        loadMyCampaigns()
    }

    private fun loadMyCampaigns() {
        viewModelScope.launch {
            campaignRepository.myCreatedCampaigns.collect { campaignIds ->
                if (campaignIds.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        myCampaigns = emptyList(),
                        isLoadingCampaigns = false
                    )
                    return@collect
                }
                try {
                    val campaigns = campaignIds.map { id ->
                        try {
                            val doc = firestore.collection("campaigns").document(id).get().await()
                            CampaignItem(
                                id = id,
                                packageName = doc.getString("packageName") ?: "",
                                googleGroupEmail = doc.getString("googleGroupEmail") ?: ""
                            )
                        } catch (_: Exception) {
                            CampaignItem(id = id)
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        myCampaigns = campaigns,
                        isLoadingCampaigns = false
                    )
                } catch (_: Exception) {
                    _uiState.value = _uiState.value.copy(isLoadingCampaigns = false)
                }
            }
        }
    }

    fun onGoogleGroupEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(googleGroupEmail = value, errorMessage = null)
    }

    fun onPackageNameChange(value: String) {
        _uiState.value = _uiState.value.copy(packageName = value, errorMessage = null)
    }

    fun createCampaign() {
        val state = _uiState.value
        if (state.googleGroupEmail.isBlank() || state.packageName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Both fields are required.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val campaignId = UUID.randomUUID().toString().take(8).uppercase()
                val campaign = hashMapOf(
                    "campaignId" to campaignId,
                    "googleGroupEmail" to state.googleGroupEmail.trim(),
                    "packageName" to state.packageName.trim(),
                    "package_names" to listOf(state.packageName.trim()),
                    "created_at" to System.currentTimeMillis(),
                    "creator_device_id" to deviceId
                )
                firestore.collection("campaigns")
                    .document(campaignId)
                    .set(campaign)
                    .await()

                campaignRepository.addCreatedCampaign(campaignId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    generatedCampaignId = campaignId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to create campaign."
                )
            }
        }
    }

    fun deleteCampaign(campaignId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("campaigns").document(campaignId).delete().await()
                campaignRepository.removeCreatedCampaign(campaignId)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete campaign."
                )
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(generatedCampaignId = null)
    }

    class Factory(
        private val campaignRepository: CampaignRepository,
        private val deviceId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreatorViewModel(campaignRepository, deviceId) as T
        }
    }
}
