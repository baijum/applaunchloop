package com.baijum.applaunchloop.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.campaignDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "campaign_preferences"
)

class CampaignRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : CampaignRepository {

    private object PreferencesKeys {
        val ACTIVE_CAMPAIGN_ID = stringPreferencesKey("active_campaign_id")
        val TARGET_PACKAGE_NAMES = stringSetPreferencesKey("target_package_names")
        val CURRENT_STREAK_COUNT = intPreferencesKey("current_streak_count")
        val LAST_RUN_TIMESTAMP = longPreferencesKey("last_run_timestamp")
        val MY_CREATED_CAMPAIGNS = stringSetPreferencesKey("my_created_campaigns")
        val LAST_DASHBOARD = stringPreferencesKey("last_dashboard")
    }

    override val activeCampaignId: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ACTIVE_CAMPAIGN_ID] ?: ""
    }

    override val targetPackageNames: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.TARGET_PACKAGE_NAMES] ?: emptySet()
    }

    override val currentStreakCount: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CURRENT_STREAK_COUNT] ?: 0
    }

    override val lastRunTimestamp: Flow<Long> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LAST_RUN_TIMESTAMP] ?: 0L
    }

    override val myCreatedCampaigns: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.MY_CREATED_CAMPAIGNS] ?: emptySet()
    }

    override val lastDashboard: Flow<String> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LAST_DASHBOARD] ?: ""
    }

    override suspend fun setActiveCampaignId(id: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.ACTIVE_CAMPAIGN_ID] = id
        }
    }

    override suspend fun setTargetPackageNames(names: Set<String>) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.TARGET_PACKAGE_NAMES] = names
        }
    }

    override suspend fun incrementStreak() {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.CURRENT_STREAK_COUNT] ?: 0
            prefs[PreferencesKeys.CURRENT_STREAK_COUNT] = (current + 1).coerceAtMost(14)
        }
    }

    override suspend fun resetStreak() {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.CURRENT_STREAK_COUNT] = 0
        }
    }

    override suspend fun updateLastRunTimestamp(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_RUN_TIMESTAMP] = timestamp
        }
    }

    override suspend fun addCreatedCampaign(campaignId: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.MY_CREATED_CAMPAIGNS] ?: emptySet()
            prefs[PreferencesKeys.MY_CREATED_CAMPAIGNS] = current + campaignId
        }
    }

    override suspend fun removeCreatedCampaign(campaignId: String) {
        dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.MY_CREATED_CAMPAIGNS] ?: emptySet()
            prefs[PreferencesKeys.MY_CREATED_CAMPAIGNS] = current - campaignId
        }
    }

    override suspend fun setLastDashboard(dashboard: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_DASHBOARD] = dashboard
        }
    }

    override suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
