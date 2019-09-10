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
import android.os.StrictMode
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.regex.Matcher
import java.util.regex.Pattern

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
        if (parentUrl.startsWith("https://www.instagram.com/")) { getInstagramUrl().execute(parentUrl) }
        if (parentUrl.startsWith("https://www.instagram.com/")) { getInstagramUrl().execute(parentUrl) }
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
                val instant = InstaEntity(0, name, postedBy, image, video, parentUrl, "", "Video", "0", "0", Legion().getCurrentDate())
                DatabaseApp().getInstaDao(applicationContext).insertInsta(instant)

                if (Constants().isAutoDownload){
                    downloadVideo(tempUrl, instant)
                }
            } else {
                tempUrl = image
                /**
                 * Save item in database
                 */
                val instant = InstaEntity(0, name, postedBy, image, video, parentUrl, "", "Image", "1", "0", Legion().getCurrentDate())
                DatabaseApp().getInstaDao(applicationContext).insertInsta(instant)
                if (Constants().isAutoDownload){
                    downloadImage(tempUrl, instant)
                }
            }

            Log.e("Insta items", DatabaseApp().getInstaDao(applicationContext).countInstaList().toString())
        }
    }

    inner class getFacebookUrl : AsyncTask<String, String, String>() {

        /* access modifiers changed from: protected */
        public override fun onPreExecute() { super.onPreExecute() }

        /* access modifiers changed from: protected */
        public override fun doInBackground(vararg strings: String): String? {
            var z = false

            val sb = StringBuilder("")
            try {
                var httpURLConnection = URL(strings[0]).openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "GET"
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                httpURLConnection.useCaches = false
                httpURLConnection.instanceFollowRedirects = true
                HttpURLConnection.setFollowRedirects(true)
                val responseCode = httpURLConnection.responseCode
                if (responseCode != 200 && (responseCode == 302 || responseCode == 301 || responseCode == 303)) { z = true }
                if (z) {
                    httpURLConnection = URL(httpURLConnection.getHeaderField("Location")).openConnection() as HttpURLConnection
                    httpURLConnection.requestMethod = "GET"
                    httpURLConnection.readTimeout = 5500
                    httpURLConnection.connectTimeout = 5500
                    httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    httpURLConnection.useCaches = false
                    httpURLConnection.instanceFollowRedirects = true
                    HttpURLConnection.setFollowRedirects(true)
                }
                val httpURLConnection2 = httpURLConnection
                return try {
                    val bufferedReader2 = BufferedReader(InputStreamReader(httpURLConnection2.getInputStream()))
                    while (true) {
                        val readLine2 = bufferedReader2.readLine() ?: break
                        sb.append(readLine2).append("\n")
                    }
                    bufferedReader2.close()
                    httpURLConnection2.disconnect()
                    sb.toString()
                } catch (e2: Exception) {
                    val str2 = ""
                    httpURLConnection2.disconnect()
                    str2
                }
            } catch (e3: IOException) {
                e3.printStackTrace()
            }

            return ""
        }

        /* access modifiers changed from: protected */
        public override fun onPostExecute(response: String) {
            super.onPostExecute(response)
            val original = response
            var sample = randomMatcher(response, "background-image:.+?url\\(&quot;(.+?)&quot;")
            sample.add(phaseTwoMatcher(mainReplacer(original), "\"dest_uri\":\"(.+?)\"").replace("\\", ""))
            var sampleTwo = phaseTwoMatcher(response, "property=\"og:description\" content=\"([^\"]+)\"")
            val a = mainReplacer(phaseTwoMatcher(response, "\"([^\"]+)\" data-sigil=\"inlineVideo\""))
            val a2 = mainReplacer(phaseTwoMatcher(response, "scaledImageFitHeight img\" src=\"([^\"]+)\""))
            val a3 = mainReplacer(phaseTwoMatcher(response, "data-store=\"([^\"]+imgsrc[^\"]+)\""))
            val b = phaseTwoMatcher(original, "class=\"_4o54\".+?&amp;url=(.+?)&")

            Log.e("facebook items", original)
            Log.e("facebook items", sample.toString())
            Log.e("facebook items", sampleTwo)
            Log.e("facebook items", a)
            Log.e("facebook items", a2)
            Log.e("facebook items", a3)
            Log.e("facebook items", b)
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

    fun randomMatcher(str: String, str2: String): ArrayList<String> {
        val matcher: Matcher = Pattern.compile(str2).matcher(str)
        val arrayList = ArrayList<String>()
        while (matcher.find()){ arrayList.add(matcher.group(1)) }
        return arrayList
    }
    
    fun mainReplacer(str: String): String {
        return str.replace("&#123;", "{").replace("&#125;", "}").replace("&amp;", "&").replace("&gt;", ">").replace("&lt;", "<").replace("&quot;", "\"").replace("&apos;", "'")
    }

    fun phaseTwoMatcher(str: String, str2: String): String {
        val matcher: Matcher = Pattern.compile(str2).matcher(str)
        return if (matcher.find()){ matcher.group(1) }else{ "" }
    }


}