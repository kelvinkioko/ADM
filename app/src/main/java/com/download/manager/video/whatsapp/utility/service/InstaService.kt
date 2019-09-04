package com.download.manager.video.whatsapp.utility.service

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.DownloadManager
import android.app.DownloadManager.Request
import android.app.Service
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

class InstaService : IntentService("InstaService") {

    private var postedBy: String = ""
    private var image: String = ""
    private var name: String = ""
    private var tempUrl: String = ""
    private var type: String = ""
    private var video: String = ""

    private var isError: Boolean = false
    private var isVideo: Boolean = false

    private var action: String = ""
    private val ACTION_START = "com.download.manager.video.whatsapp.action.START"
    private val ACTION_STOP = "com.download.manager.video.whatsapp.action.STOP"

    var ClipboardListener: OnPrimaryClipChangedListener = OnPrimaryClipChangedListener {
        val s = (getSystemService("clipboard") as ClipboardManager).primaryClip.getItemAt(0).text.toString()
        if (s.startsWith("https://www.instagram.com/")) { GetUrl().execute(s) }
    }

    override fun onHandleIntent(intent: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    inner class GetUrl : AsyncTask<String, String, String>() {

        /* access modifiers changed from: protected */
        public override fun onPreExecute() {
            super.onPreExecute()
        }

        /* access modifiers changed from: protected */
        public override fun doInBackground(vararg strings: String): String? {
            try {
                val doc = Jsoup.connect(strings[0]).get()
                this@InstaService.image = doc.select("meta[property=og:image]").attr("content")
                this@InstaService.video = doc.select("meta[property=og:video:secure_url]").attr("content")
                this@InstaService.postedBy = doc.select("meta[property=og:description]").attr("content").split("@")[1].split("•")[0].trim()
                this@InstaService.name = (Random().nextInt(899999999) as Long).toString()
                this@InstaService.isVideo = java.lang.Boolean.valueOf(!this@InstaService.video.equals(""))
            } catch (e: IOException) {
                isError = java.lang.Boolean.valueOf(true)
                isVideo = java.lang.Boolean.valueOf(false)
                e.printStackTrace()
            } catch (e2: Exception) {
                isError = java.lang.Boolean.valueOf(true)
                isVideo = java.lang.Boolean.valueOf(false)
                e2.printStackTrace()
            }

            return null
        }

        /* access modifiers changed from: protected */
        public override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            if (!Constants().isAutoDownload) {
                if (isVideo) {
                    tempUrl = this@InstaService.video
                    /**
                     * Save item in database
                     */
                } else {
                    tempUrl = this@InstaService.image
                    /**
                     * Save item in database
                     */
                }

            } else if (isVideo) {
                tempUrl = this@InstaService.video
                /**
                 * Save item in database
                 */

                /**
                 * Download video
                 */
            } else {
                tempUrl = this@InstaService.image
                /**
                 * Save item in database
                 */

                /**
                 * Download image
                 */
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun downloadImage(url: String, instant: InstaEntity) {
        val name = instant.name
        val manager = getSystemService("download") as DownloadManager
        val request = Request(Uri.parse(url))
        request.setAllowedNetworkTypes(3).setAllowedOverRoaming(false).setTitle("Download Manager")
            .setDescription("Downloading:$name.jpeg").setDestinationInExternalPublicDir("", "/Download Manager/insta/images/$name.jpeg")
        manager.enqueue(request)
        /**
         * insert download to database
         */
    }

    @SuppressLint("WrongConstant")
    fun downloadVideo(uRl: String, instant: InstaEntity) {
        val name = instant.name
        val manager = getSystemService("download") as DownloadManager
        val request = Request(Uri.parse(uRl))
        request.setAllowedNetworkTypes(3).setAllowedOverRoaming(false).setTitle("Download Manager")
            .setDescription("Downloading:$name.mp4").setDestinationInExternalPublicDir("", "/Download Manager/insta/videos/$name.mp4")
        manager.enqueue(request)
        /**
         * insert download to database
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

    private fun processNotificationShowRequest() {
//        startForeground(101, this.notification.build())
//        startForeground(101, )
    }

}