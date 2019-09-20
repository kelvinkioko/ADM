package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.adapter.DownloadsAdapter
import com.download.manager.video.whatsapp.database.adapter.SectionableAdapter
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.ui.MainActivity
import com.download.manager.video.whatsapp.utility.Downloader
import com.download.manager.video.whatsapp.utility.VideoContentSearch
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import com.download.manager.video.whatsapp.widgets.web.ScriptUtil
import kotlinx.android.synthetic.main.dialog_add_url.*
import kotlinx.android.synthetic.main.main_downloads.*
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory
import kotlin.collections.ArrayList

class Downloads : Fragment(){

    private var defaultSSLSF: SSLSocketFactory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        PermissionListener(activity as MainActivity).loadPermissions()

        defaultSSLSF = HttpsURLConnection.getDefaultSSLSocketFactory()

        webview.settings.javaScriptEnabled = true
        webview.addJavascriptInterface(this, "browser")
        webview.webViewClient = webViewClient
        webview.webChromeClient = webChromeClient

        search_box.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->

            val query = search_box.text.toString().trim()
            if(actionId== EditorInfo.IME_ACTION_GO) {
                if (query.isEmpty()) {
                    Toast.makeText(activity, "Please enter a valid url", Toast.LENGTH_LONG).show()
                    return@OnEditorActionListener true
                }else{
                    if (Patterns.WEB_URL.matcher(query).matches()){
                        webview.loadUrl(query)
                    }else{

                        webview.loadUrl("https://www.google.com/search?q=$query")
                    }
                }
            }

            false
        })

    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_downloads, container, false)
    }

    private var webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            Log.e("progress", newProgress.toString())
            super.onProgressChanged(view, newProgress)
        }
    }

    private var webViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            search_box.setText(url)
            super.onPageStarted(view, url, favicon)
        }


        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
            search_box.setText(url.toString())
            view.loadUrl(url)
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun onLoadResource(view: WebView, url: String?) {
            super.onLoadResource(view, url)
            try {
                if (url.toString().contains("facebook.com")) {
                    view.loadUrl(ScriptUtil.FACEBOOK_SCRIPT)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val page = view.url
            val title = view.title
            object : VideoContentSearch(activity, url, page, title) {
                override fun onStartInspectingURL() {
                    Log.e("Video check start", "Hopefully we find something")
                    Handler(Looper.getMainLooper()).post {
                        /**
                         * Display loader to show searching for video link
                         */
                    }
//                    Utils.disableSSLCertificateChecking()
                }

                override fun onFinishedInspectingURL(finishedAll: Boolean) {
                    Log.e("Video Check end", "Hopefully we found something")
                    HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF)
                    if (finishedAll) {
                        Handler(Looper.getMainLooper()).post {
                            /**
                             * Hide loader to show searching for video link
                             */
                        }
                    }
                }

                override fun onVideoFound(size: String, type: String, link: String, name: String, page: String, chunked: Boolean, website: String) {
                    Log.e("Size", size)
                    Log.e("type", type)
                    Log.e("link", link)
                    Log.e("name", name)
                    Log.e("page", page)
                    Log.e("chunked", chunked.toString())
                    Log.e("website", website)
                }
            }.start()
        }

        override fun onPageFinished(view: WebView, url: String) {
            Log.e("progress", "finished")
            super.onPageFinished(view, url)
        }
    }

}