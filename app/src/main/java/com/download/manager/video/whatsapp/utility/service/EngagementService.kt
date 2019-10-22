package com.download.manager.video.whatsapp.utility.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.ui.MainActivity

class EngagementService : JobService(){
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        notifyDownloadComplete()
        return false
    }

    private fun notifyDownloadComplete(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("status", "1")
        val title = "Android Download Manager"
        val text = "New posts have been detected on whatsapp and Instagram. Tap to view and download"

        val msg = resources.getString(R.string.share_message) + " "
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, msg + "https://play.google.com/store/apps/details?id=" + packageName)
        shareIntent.type = "text/plain"

        val builder = NotificationCompat.Builder(applicationContext, "DownloadManager0512")
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_settings_rate, "View", PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            .addAction(R.drawable.ic_settings_rate, "Share", PendingIntent.getActivity(applicationContext, 0, Intent.createChooser(shareIntent, "Share..."), PendingIntent.FLAG_UPDATE_CURRENT))
        notify(builder.build())
    }

    private fun notify(notification : Notification){
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("DownloadManager0512", "Download complete!", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify("Download Manager", 0, notification)
    }

}