package com.download.manager.video.whatsapp.utility.service

import android.annotation.SuppressLint
import android.app.DownloadManager.Request
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import com.download.manager.video.whatsapp.engine.Constants
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.ui.MainActivity
import android.annotation.TargetApi
import android.app.*
import android.util.Log
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.engine.Legion
import android.content.Context


class InstaService : IntentService("InstaService") {

    private var parentUrl: String = ""
    private var postedBy: String = ""
    private var image: String = ""
    private var name: String = ""
    private var tempUrl: String = ""
    private var type: String = ""
    private var video: String = ""

    private lateinit var notification: Notification.Builder

    private var isError: Boolean = false
    private var isVideo: Boolean = false

    private var action: String = ""
    val ACTION_START = "com.download.manager.video.whatsapp.action.START"
    private val ACTION_STOP = "com.download.manager.video.whatsapp.action.STOP"

    private var ClipboardListener: OnPrimaryClipChangedListener = OnPrimaryClipChangedListener {
        parentUrl = (getSystemService("clipboard") as ClipboardManager).primaryClip.getItemAt(0).text.toString()
        if (parentUrl.startsWith("https://www.instagram.com/")) { GetUrl().execute(parentUrl) }
    }

    override fun onHandleIntent(intent: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class GetUrl : AsyncTask<String, String, String>() {

        /* access modifiers changed from: protected */
        public override fun onPreExecute() { super.onPreExecute() }

        /* access modifiers changed from: protected */
        public override fun doInBackground(vararg strings: String): String? {
            try {
                val doc = Jsoup.connect(strings[0]).get()
                this@InstaService.image = doc.select("meta[property=og:image]").attr("content")
                this@InstaService.video = doc.select("meta[property=og:video:secure_url]").attr("content")
                this@InstaService.postedBy = doc.select("meta[property=og:description]").attr("content").split("@")[1].split("•")[0].trim()
                this@InstaService.name = (Random().nextInt(899999999)).toString()
                this@InstaService.isVideo = video.isNotEmpty()
                Log.e("Final URL iMAGE", "$video - $image")
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
            if (isVideo) {
                tempUrl = video
                /**
                 * Save item in database
                 */
                val instant = InstaEntity(0, name, postedBy, image, video, parentUrl, "", "Video", "1", Legion().getCurrentDateTime())
                DatabaseApp().getInstaDao(applicationContext).insertInsta(instant)

                if (Constants().isAutoDownload){
                    downloadVideo(tempUrl, instant)
                }
            } else {
                tempUrl = image
                /**
                 * Save item in database
                 */
                val instant = InstaEntity(0, name, postedBy, image, video, parentUrl, "", "Image", "1", Legion().getCurrentDateTime())
                DatabaseApp().getInstaDao(applicationContext).insertInsta(instant)
                if (Constants().isAutoDownload){
                    downloadImage(tempUrl, instant)
                }
            }

            Log.e("Insta items", DatabaseApp().getInstaDao(applicationContext).countInstaList().toString())
        }
    }

    fun downloadImage(url: String, instant: InstaEntity) {
        val name = instant.name
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = Request(Uri.parse(url))
        request.setAllowedNetworkTypes(Request.NETWORK_WIFI or Request.NETWORK_MOBILE).setAllowedOverRoaming(true).setTitle("Download Manager")
            .setDescription("Downloading:$name.jpeg").setDestinationInExternalPublicDir("", "/Download Manager/insta/images/$name.jpeg")
        manager.enqueue(request)
        /**
         * update database details after download
         */
    }

    fun downloadVideo(uRl: String, instant: InstaEntity) {
        val name = instant.name
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = Request(Uri.parse(uRl))
        request.setAllowedNetworkTypes(Request.NETWORK_WIFI or Request.NETWORK_MOBILE).setAllowedOverRoaming(false).setTitle("Download Manager")
            .setDescription("Downloading:$name.mp4").setDestinationInExternalPublicDir("", "/Download Manager/insta/videos/$name.mp4")
        manager.enqueue(request)
        /**
         * update database details after download
         */
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.action = intent!!.action
        if (this.action.equals(ACTION_START)) {
            processNotificationShowRequest()
            Constants().isService = true
        } else if (this.action.equals(ACTION_STOP)) {
            stopService(intent)
            stopForeground(true)
            Constants().isService = false
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    @TargetApi(16)
    override fun onCreate() {
        super.onCreate()
        /**
         * Initialize the database
         */

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        notificationIntent.putExtra("from", "service")
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val stopIntent = Intent(this, InstaService::class.java)
        stopIntent.action = ACTION_STOP
        this.notification = Notification.Builder(this).setContentTitle("Android Download Manager").setTicker("Android Download Manager").setContentText("Just Copy Url From Instagram")
            .setSmallIcon(R.drawable.icon_cancel).setPriority(Notification.PRIORITY_HIGH).setContentIntent(pendingIntent).setOngoing(true)
            .addAction(R.drawable.icon_cancel, "Stop", PendingIntent.getService(this, 0, stopIntent, 0))
        (getSystemService("clipboard") as ClipboardManager).addPrimaryClipChangedListener(this.ClipboardListener)
    }

    private fun processNotificationShowRequest() {
        startForeground(101, this.notification.build())
    }

}