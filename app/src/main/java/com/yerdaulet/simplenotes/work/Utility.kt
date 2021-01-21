package com.yerdaulet.simplenotes.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.yerdaulet.simplenotes.domain.Note
import com.yerdaulet.simplenotes.util.NOTE_ID
import com.yerdaulet.simplenotes.util.NOTE_TITLE
import com.yerdaulet.simplenotes.util.currentDate
import java.util.concurrent.TimeUnit

fun createSchedule(context: Context, note: Note) {
    val data = Data.Builder()
            .putInt(NOTE_ID, note.id!!)
            .putString(NOTE_TITLE, note.title)
            .build()

    val delay = note.reminder!! - currentDate().timeInMillis

    note.started = true

    scheduleReminder(delay, data, context)

}

fun scheduleReminder(delay: Long, data: Data, context: Context) {
    val reminderWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("${context.packageName}.work.Notifywork")
            .build()

    val workName = "Work ${data.getInt(NOTE_ID, 0)}"

    val instanceWorkManager = WorkManager.getInstance(context)
    instanceWorkManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, reminderWork)
}

fun cancelAlarm(context: Context, note: Note){
    val workName = "Work ${note.id}"
    val instanceWorkManager = WorkManager.getInstance(context)
    instanceWorkManager.cancelUniqueWork(workName)
}
