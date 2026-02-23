package com.baijum.applaunchloop

import android.app.Application
import com.baijum.applaunchloop.data.repository.CampaignRepository
import com.baijum.applaunchloop.data.repository.CampaignRepositoryImpl
import com.baijum.applaunchloop.data.repository.campaignDataStore
import com.baijum.applaunchloop.worker.DailyTestWorker

class AppLaunchLoopApplication : Application() {

    lateinit var campaignRepository: CampaignRepository
        private set

    override fun onCreate() {
        super.onCreate()
        campaignRepository = CampaignRepositoryImpl(campaignDataStore)
        DailyTestWorker.createNotificationChannel(this)
    }
}
