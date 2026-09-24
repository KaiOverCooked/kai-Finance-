package com.example.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.KaiDatabase
import com.example.data.repository.FinanceRepository
import com.example.data.scheduler.RecurringScheduler
import com.example.data.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecurringAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = KaiDatabase.getDatabase(context)
                val repository = FinanceRepository(db)
                val executedCount = repository.processDueRecurringTransactions()

                if (executedCount > 0) {
                    NotificationHelper.showNotification(
                        context,
                        "Transaksi Otomatis Dieksekusi",
                        "$executedCount transaksi rutin yang jatuh tempo telah otomatis dicatat ke akun terkait."
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                RecurringScheduler.schedulePeriodicCheck(context)
                pendingResult.finish()
            }
        }
    }
}
