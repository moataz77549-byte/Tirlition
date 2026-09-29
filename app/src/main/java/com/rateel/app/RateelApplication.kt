package com.rateel.app

import android.app.Application
import com.rateel.app.domain.repository.SourceRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class RateelApplication : Application(), androidx.work.Configuration.Provider {
    @Inject lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory
    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder().setWorkerFactory(workerFactory).build()
    @Inject lateinit var sourceRepository: SourceRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            sourceRepository.ensureBuiltInCatalog()
        }
    }
}
