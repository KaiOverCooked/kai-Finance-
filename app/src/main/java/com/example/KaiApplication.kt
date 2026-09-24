package com.example

import android.app.Application
import com.example.data.local.KaiDatabase
import com.example.data.repository.FinanceRepository
import com.example.data.scheduler.RecurringScheduler
import com.example.data.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KaiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        RecurringScheduler.schedulePeriodicCheck(this)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = KaiDatabase.getDatabase(this@KaiApplication)
                val repository = FinanceRepository(db)
                repository.seedInitialDataIfEmpty()
                repository.processDueRecurringTransactions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
