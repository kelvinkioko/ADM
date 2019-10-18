package com.download.manager.video.whatsapp.utility.service

import android.app.DownloadManager.Request
import android.content.ClipboardManager
import android.content.ClipboardManager.OnPrimaryClipChangedListener
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import org.jsoup.Jsoup
import java.util.*
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.ui.MainActivity
import android.annotation.TargetApi
import android.app.*
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.engine.Legion
import android.content.Context
import android.os.Build
import android.os.Environment
import android.support.v4.app.NotificationCompat
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

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
        parentUrl = (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip?.getItemAt(0)?.text.toString()

        if (parentUrl.isNotEmpty() && parentUrl.startsWith("https://www.instagram.com/")) { getInstagramUrl().execute(parentUrl) }
    }

    override fun onHandleIntent(intent: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class getInstagramUrl : AsyncTask<String, String, String>() {

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
                DatabaseApp().getInstaDao(applicationContext).updateInstaDetails(name, postedBy, video, "Video", parentInsta.id); downloadFile(video)
            } else {
                DatabaseApp().getInstaDao(applicationContext).updateInstaDetails(name, postedBy, image, "Image", parentInsta.id); downloadFile(image)
            }
        }
    }

    fun downloadFile(url: String){
        val item = DatabaseApp().getInstaDao(applicationContext).getInstaByParent(parentUrl)
        val downloader = Downloader.Builder(applicationContext, url, "Insta:" + item.type)
            .downloadListener(object : OnDownloadListener {
                override fun onStart() {}

                override fun onPause() {}

                override fun onResume() {}

                override fun onProgressUpdate(percent: Int, downloadedSize: Int, totalSize: Int) {}

                override fun onCompleted(file: File?) {
                    val final = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Android Download Manager" + File.separator + "Instagram ADM" + File.separator + file?.name)

                    if (!final.parentFile.exists()) { final.parentFile.mkdirs() }
                    if (!final.exists()) { final.createNewFile() }

                    var source: FileChannel? = null
                    var destination: FileChannel? = null

                    try {
                        source = FileInputStream(file).channel
                        destination = FileOutputStream(final).channel
                        destination!!.transferFrom(source, 0, source!!.size())
                    } finally {
                        file?.delete()
                        source?.close()
                        destination?.close()
                    }

                    DatabaseApp().getInstaDao(applicationContext).updateLocalURL(final.toString(), item.id)
                    notifyDownloadComplete()

                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    mediaScanIntent.data = Uri.fromFile(final)
                    sendBroadcast(mediaScanIntent)
                }

                override fun onFailure(reason: String?) {}

                override fun onCancel() {}
            }).build()
        downloader.download()
    }

    private fun notifyDownloadComplete(){
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra("status", "1")
        val title = "Android Download Manager"
        val text = "Your IG download is complete. Tap to view"

        val google_play_url = "https://play.google.com/store/apps/details?id="

        val msg = resources.getString(R.string.share_message) + " "
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_TEXT, msg + google_play_url + packageName)
        shareIntent.type = "text/plain"

        val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        val ratePendingIntent = PendingIntent.getActivity(this, 0, rateIntent, 0)

        val builder = NotificationCompat.Builder(applicationContext, "DownloadManager1292")
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_settings_rate, "Rate", ratePendingIntent)
            .addAction(R.drawable.ic_settings_rate, "Share", PendingIntent.getActivity(applicationContext, 0, Intent.createChooser(shareIntent, "Share..."), PendingIntent.FLAG_UPDATE_CURRENT))
        notify(builder.build())
    }

    private fun notify(notification :Notification){
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("DownloadManager1292", "Download complete!", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify("Download Manager", 0, notification)
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
//        this.action = intent!!.action
//        if (this.action.equals(ACTION_START)) {
//            processNotificationShowRequest()
//            Constants().isService = true
//        } else if (this.action.equals(ACTION_STOP)) {
//            stopService(intent)
//            stopForeground(true)
//            Constants().isService = false
//        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    @TargetApi(16)
    override fun onCreate() {
        super.onCreate()
        /*** Initialize the database */

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
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).addPrimaryClipChangedListener(this.ClipboardListener)
    }

    private fun processNotificationShowRequest() {
        startForeground(101, this.notification.build())
    }

}