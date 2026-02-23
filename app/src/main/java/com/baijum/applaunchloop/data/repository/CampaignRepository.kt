package com.baijum.applaunchloop.data.repository

import kotlinx.coroutines.flow.Flow

interface CampaignRepository {
    val activeCampaignId: Flow<String>
    val targetPackageNames: Flow<Set<String>>
    val currentStreakCount: Flow<Int>
    val lastRunTimestamp: Flow<Long>
    val myCreatedCampaigns: Flow<Set<String>>
    val lastDashboard: Flow<String>

    suspend fun setActiveCampaignId(id: String)
    suspend fun setTargetPackageNames(names: Set<String>)
    suspend fun incrementStreak()
    suspend fun resetStreak()
    suspend fun updateLastRunTimestamp(timestamp: Long)
    suspend fun addCreatedCampaign(campaignId: String)
    suspend fun removeCreatedCampaign(campaignId: String)
    suspend fun setLastDashboard(dashboard: String)
    suspend fun clearAll()
}
