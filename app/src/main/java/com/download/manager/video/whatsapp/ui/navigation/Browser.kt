package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.Patterns
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.adapter.DownloadListAdapter
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.ui.DownloadsActivity
import com.download.manager.video.whatsapp.ui.MainActivity
import com.download.manager.video.whatsapp.utility.VideoContentSearch
import com.download.manager.video.whatsapp.widgets.web.ScriptUtil
import kotlinx.android.synthetic.main.dialog_links.*
import kotlinx.android.synthetic.main.main_browser.*
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory
import kotlin.collections.ArrayList

class Browser : Fragment(){

    private lateinit var dialog: Dialog
    private var defaultSSLSF: SSLSocketFactory? = null
    private lateinit var downloadsViewModel: DownloadsViewModel
    private var downloadsEntity: MutableList<DownloadsEntity> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

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
                    val aniRotateClk = AnimationUtils.loadAnimation(activity as MainActivity, R.anim.rotation)
                    iv_refresh.startAnimation(aniRotateClk)
                    if (Patterns.WEB_URL.matcher(query).matches()){
                        webview.loadUrl(query)
                    }else{
                        webview.loadUrl("https://www.google.com/search?q=$query")
                    }
                }
            }

            false
        })

        iv_back.setOnClickListener {
            if (webview.canGoBack()){
                webview.goBack()
                downloadsEntity.clear()
            }
        }

        iv_downloads.setOnClickListener {
            startActivity(Intent(activity, DownloadsActivity::class.java))
        }

        downloads_parent.setOnClickListener {
            dialog = Dialog(activity)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_links)
            Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.show()

            val _links: RecyclerView = dialog.download_links
            var _dismiss: TextView = dialog.dl_dismiss

            val downloadsListAdapter = DownloadListAdapter(activity as MainActivity, downloadsEntity)
            val linksManager = LinearLayoutManager(activity as MainActivity, LinearLayoutManager.VERTICAL, false)
            _links.layoutManager = linksManager
            _links.itemAnimator = DefaultItemAnimator()
            _links.adapter = downloadsListAdapter

            downloadsListAdapter.setList(downloadsEntity)

            _dismiss.setOnClickListener { dialog.dismiss() }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_browser, container, false)
    }

    private var webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            iv_refresh.startAnimation(AnimationUtils.loadAnimation(activity as MainActivity, R.anim.rotation))
            if (newProgress == 100){ downloadsEntity.clear(); iv_refresh.clearAnimation()}
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
            search_box.setText(view.url.toString())
            val page = view.url
            val title = view.title
            object : VideoContentSearch(activity, url, page, title) {
                override fun onStartInspectingURL() {
                    Handler(Looper.getMainLooper()).post {
                        /**
                         * Display loader to show searching for video link
                         */
                        downloads_loader.visibility = View.VISIBLE
                    }
                }

                override fun onFinishedInspectingURL(finishedAll: Boolean) {
                    HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF)
                    if (finishedAll) {
                        Handler(Looper.getMainLooper()).post {
                            /**
                             * Hide loader to show searching for video link
                             */
                            downloads_loader.visibility = View.GONE
                            downloads_counter.text = downloadsEntity.size.toString()
                        }
                    }
                }

                override fun onVideoFound(size: String, type: String, link: String, name: String, page: String, chunked: Boolean, website: String) {
                    if (!size.equals("0")) {
                        val download = DownloadsEntity(0, name, link, "", "1", size, "")
                        if(!downloadsEntity.contains(download)) {
                            downloadsEntity.add(download)
                        }
                    }
                    Handler(Looper.getMainLooper()).post {
                        /**
                         * update counter
                         */
                        downloads_counter.text = downloadsEntity.size.toString()
                    }

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
            iv_refresh.clearAnimation()
            super.onPageFinished(view, url)
        }
    }


}