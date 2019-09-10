package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.ui.MainActivity
import com.download.manager.video.whatsapp.database.adapter.InstaAdapter
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.utility.service.InstaService
import kotlinx.android.synthetic.main.dialog_add_url.*
import kotlinx.android.synthetic.main.main_downloads.*
import kotlinx.android.synthetic.main.main_facebook.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class Facebook : Fragment(), InstaAdapter.OnItemClickListener  {

    override fun parentClick(view: View, position: Int, userCode: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var downloadsViewModel: DownloadsViewModel
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar!!.title = "Home | FB"

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        main_add_facebook.setOnClickListener{
            dialog = Dialog(activity)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_add_url)
            Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.show()

            val link = dialog.dau_link
            val dismiss = dialog.dau_dismiss
            val done = dialog.dau_done

            dismiss.setOnClickListener {
                dialog.dismiss()
            }

            done.setOnClickListener {
                if (link.text.toString().trim().startsWith("https://m.facebook.")) { getFacebookUrl().execute(link.text.toString().trim()) }
                dialog.dismiss()
            }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_facebook, container, false)

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

    override fun onResume() {
        super.onResume()
//        populateDownloads()
    }

}