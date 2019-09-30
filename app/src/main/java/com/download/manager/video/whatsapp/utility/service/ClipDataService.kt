package com.download.manager.video.whatsapp.utility.service

import android.annotation.TargetApi
import android.app.Notification
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ClipboardManager
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.ui.MainActivity
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*

class ClipDataService : JobService(){

    private var parentUrl: String = ""; private var postedBy: String = ""; private var image: String = ""
    private var name: String = ""; private var video: String = ""
    private var isError: Boolean = false; private var isVideo: Boolean = false

    private lateinit var notification: Notification.Builder
    val ACTION_START = "com.download.manager.video.whatsapp.action.START"
    private val ACTION_STOP = "com.download.manager.video.whatsapp.action.STOP"

    private var ClipboardListener: ClipboardManager.OnPrimaryClipChangedListener =
        ClipboardManager.OnPrimaryClipChangedListener {
            val parentUrl = (getSystemService("clipboard") as ClipboardManager).primaryClip.getItemAt(0).text.toString()

            if (parentUrl.startsWith("https://www.instagram.com/") && DatabaseApp().getInstaDao(applicationContext).countInstaListByParent(parentUrl) == 0) {
                val instant = InstaEntity(0, "", "", parentUrl, "", "", "", "0", "0", Legion().getCurrentDate())
                DatabaseApp().getInstaDao(applicationContext).insertInsta(instant)
                getInstagramUrl().execute(parentUrl)
                Log.e("clip url", parentUrl)
            }
        }

    override fun onStartJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onCreate() {
        super.onCreate()

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        notificationIntent.putExtra("from", "service")
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val stopIntent = Intent(this, ClipDataService::class.java)
        stopIntent.action = ACTION_STOP

        this.notification = Notification
            .Builder(this)
            .setContentTitle("Download Manager")
            .setTicker("Download Manager")
            .setContentText("Just Copy Url From Instagram")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(R.mipmap.ic_launcher, "Stop", PendingIntent.getService(this, 0, stopIntent, 0))

        (getSystemService("clipboard") as ClipboardManager).addPrimaryClipChangedListener(this.ClipboardListener)
    }

    inner class getInstagramUrl : AsyncTask<String, String, String>() {

        /* access modifiers changed from: protected */
        public override fun onPreExecute() { super.onPreExecute() }

        /* access modifiers changed from: protected */
        public override fun doInBackground(vararg strings: String): String? {
            try {
                val doc = Jsoup.connect(strings[0]).get()
                this@ClipDataService.image = doc.select("meta[property=og:image]").attr("content")
                this@ClipDataService.video = doc.select("meta[property=og:video:secure_url]").attr("content")
                this@ClipDataService.postedBy = doc.select("meta[property=og:description]").attr("content").split("@")[1].split("•")[0].trim()
                this@ClipDataService.name = (Random().nextInt(899999999)).toString()
                this@ClipDataService.isVideo = video.isNotEmpty()
            } catch (e: IOException) {
                isError = true
                isVideo = false
                e.printStackTrace()
            }
            return ""
        }

        /* access modifiers changed from: protected */
        public override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            val parentInsta = DatabaseApp().getInstaDao(applicationContext).getInstaByParent(parentUrl)
            if (isVideo) {
                DatabaseApp().getInstaDao(applicationContext).updateInstaDetails(name, postedBy, video, "Video", parentInsta.id)
                Log.e("clip url", video)
            } else {
                DatabaseApp().getInstaDao(applicationContext).updateInstaDetails(name, postedBy, image, "Image", parentInsta.id)
                Log.e("clip url", image)
            }
        }
    }
}