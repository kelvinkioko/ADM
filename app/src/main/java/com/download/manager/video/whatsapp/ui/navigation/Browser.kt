package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Patterns
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.adapter.BookmarkAdapter
import com.download.manager.video.whatsapp.database.adapter.DownloadListAdapter
import com.download.manager.video.whatsapp.database.entity.BookmarkEntity
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.engine.RecyclerTouchListener
import com.download.manager.video.whatsapp.ui.MainActivity
import com.download.manager.video.whatsapp.utility.VideoContentSearch
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_how_to_browser.*
import kotlinx.android.synthetic.main.dialog_links.*
import kotlinx.android.synthetic.main.dialog_restriction.*
import kotlinx.android.synthetic.main.dialog_save_download.*
import kotlinx.android.synthetic.main.item_album.view.*
import kotlinx.android.synthetic.main.main_browser.*
import kotlinx.android.synthetic.main.main_browser.view.*
import java.util.*

import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory
import kotlin.collections.ArrayList

class Browser : Fragment(){

    private lateinit var dialog: Dialog
    private lateinit var saveDialog: Dialog
    private var defaultSSLSF: SSLSocketFactory? = null
    private lateinit var downloadsViewModel: DownloadsViewModel
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private var downloadsEntity: MutableList<DownloadsEntity> = ArrayList()
    private var bookmarkEntity: MutableList<BookmarkEntity> = ArrayList()

    private var root: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        bookmarkAdapter = BookmarkAdapter(activity as MainActivity, bookmarkEntity)
        val linksManager = GridLayoutManager(activity as MainActivity, 3, GridLayoutManager.VERTICAL, false)
        web_history.layoutManager = linksManager
        web_history.itemAnimator = DefaultItemAnimator()
        web_history.adapter = bookmarkAdapter

        populateBookmarks()

        web_history.addOnItemTouchListener(RecyclerTouchListener(this.requireActivity(), web_history, object : RecyclerTouchListener.OnItemClickListener {
            override fun onItemClick(viewClick: View, position: Int) {
                val bookmarkIcon = viewClick.album_cover
                val bookmarkName = viewClick.album_title
                val bookmarkUrl = viewClick.album_url

                web_history.visibility = View.GONE
                webview.visibility = View.VISIBLE
                webview.loadUrl(bookmarkUrl.text.toString())
            }

            override fun onItemLongClick(view: View?, position: Int) {}
        }))

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
                    web_history.visibility = View.GONE
                    webview.visibility = View.VISIBLE
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

        iv_home.setOnClickListener{
            web_history.visibility = View.VISIBLE
            webview.visibility = View.GONE
            search_box.setText("")
        }

        downloads_parent.setOnClickListener {
            if (search_box.text.toString().trim().contains("youtube.com")){
                dialog = Dialog(activity)
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(true)
                dialog.setContentView(R.layout.dialog_restriction)
                Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                dialog.window!!.setGravity(Gravity.BOTTOM)
                dialog.show()

                val _dismiss: TextView = dialog.dr_dismiss

                _dismiss.setOnClickListener { dialog.dismiss() }
            }else {
                dialog = Dialog(activity)
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(true)
                dialog.setContentView(R.layout.dialog_links)
                Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                dialog.window!!.setGravity(Gravity.BOTTOM)
                dialog.show()

                val _links: RecyclerView = dialog.download_links
                val _dismiss: TextView = dialog.dl_dismiss

                val downloadsListAdapter = DownloadListAdapter(activity as MainActivity, downloadsEntity)
                val linksManager = LinearLayoutManager(activity as MainActivity, LinearLayoutManager.VERTICAL, false)
                _links.layoutManager = linksManager
                _links.itemAnimator = DefaultItemAnimator()
                _links.adapter = downloadsListAdapter

                downloadsListAdapter.setList(downloadsEntity)

                _links.addOnItemTouchListener(
                    RecyclerTouchListener(
                        this.requireActivity(),
                        _links,
                        object : RecyclerTouchListener.OnItemClickListener {
                            override fun onItemClick(viewClick: View, position: Int) {
                                queueDownload(downloadsEntity[position])
                            }

                            override fun onItemLongClick(view: View?, position: Int) {}
                        })
                )

                _dismiss.setOnClickListener { dialog.dismiss() }
            }
        }

        downloads_help.setOnClickListener {
            saveDialog = Dialog(activity)
            saveDialog.setCanceledOnTouchOutside(false)
            saveDialog.setCancelable(true)
            saveDialog.setContentView(R.layout.dialog_how_to_browser)
            Objects.requireNonNull<Window>(saveDialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            saveDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            saveDialog.window!!.setGravity(Gravity.BOTTOM)
            saveDialog.show()

            val dismiss = saveDialog.dc_dismiss

            dismiss.setOnClickListener { saveDialog.dismiss() }
        }
    }

    lateinit var _primary: LinearLayout
    lateinit var _success: LinearLayout

    private fun queueDownload(download: DownloadsEntity){
        saveDialog = Dialog(activity)
        saveDialog.setCanceledOnTouchOutside(false)
        saveDialog.setCancelable(true)
        saveDialog.setContentView(R.layout.dialog_save_download)
        Objects.requireNonNull<Window>(saveDialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        saveDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        saveDialog.window!!.setGravity(Gravity.BOTTOM)
        saveDialog.show()

        _primary = saveDialog.dsd_primary
        _success = saveDialog.dsd_success

        _success.visibility = View.GONE

        val title = saveDialog.dau_title
        val link_parent = saveDialog.dau_link_parent
        val name = saveDialog.dsd_name
        val dismiss = saveDialog.dsd_dismiss
        val done = saveDialog.dsd_done

        val success_message = saveDialog.dsd_success_message
        val success_dismiss = saveDialog.dsd_success_dismiss


        success_dismiss.setOnClickListener { saveDialog.dismiss() }
        dismiss.setOnClickListener { saveDialog.dismiss() }

        done.setOnClickListener {
            _primary.visibility = View.GONE
            _success.visibility = View.VISIBLE
            if (name.text.toString().trim().isEmpty()) {
                name.error = "Please set your preferred file name"
            }else {
                val downlod = DownloadsEntity(0, name.text.toString().trim(), download.url, "", "0", download.size, Legion().getCurrentDate())
                DatabaseApp().getDownloadsDao(activity as MainActivity).insertDownloads(downlod)

                val pulse = AnimationUtils.loadAnimation(activity as MainActivity, R.anim.pulse_fade_in)
                pulse.repeatCount = Animation.INFINITE
//                iv_downloads.animation = pulse
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.main_browser, container, false)  // initialize it here
        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // attempting to save the webview state for later
        root!!.webview.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        root!!.webview.onPause()
        super.onPause()
    }

    override fun onResume() {
        root!!.webview.onResume()
        super.onResume()
    }

    private var webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            root!!.iv_refresh.startAnimation(AnimationUtils.loadAnimation(activity as MainActivity, R.anim.rotation))
            if (newProgress.toString().isNotEmpty()){ if (newProgress == 100){ downloadsEntity.clear(); root!!.iv_refresh.clearAnimation()} }
            super.onProgressChanged(view, newProgress)
        }
    }

    private var webViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            if (url.toString().isNotEmpty()) {
                root!!.search_box.setText(url)
            }
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
            root!!.search_box.setText(url.toString())
            view.loadUrl(url)
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun onLoadResource(view: WebView, url: String?) {
            super.onLoadResource(view, url)
//            try {
//                if (url.toString().contains("facebook.com")) {
//                    view.loadUrl(ScriptUtil.FACEBOOK_SCRIPT)
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }

            val page = view.url
            val title = view.title
            if ((!url.toString().startsWith("https://www.youtube.com/") || !url.toString().startsWith("https://m.youtube.com/")) && url!!.isNotEmpty()) {
                object : VideoContentSearch(activity as MainActivity, url, page, title) {
                    override fun onStartInspectingURL() {
                        Handler(Looper.getMainLooper()).post {
                            /**
                             * Display loader to show searching for video link
                             */
                            root!!.downloads_loader.visibility = View.VISIBLE
                        }
                    }

                    override fun onFinishedInspectingURL(finishedAll: Boolean) {
                        HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF)
                        if (finishedAll) {
                            Handler(Looper.getMainLooper()).post {
                                /**
                                 * Hide loader to show searching for video link
                                 */
                                root!!.downloads_loader.visibility = View.GONE
                                root!!.downloads_counter.text = downloadsEntity.size.toString()
                            }
                        }
                    }

                    override fun onVideoFound(size: String, type: String, link: String,
                        name: String, page: String, chunked: Boolean, website: String) {
                        if (!size.equals("0")) {
                            val download = DownloadsEntity(0, name, link, "", "1", size, "")
                            if (!downloadsEntity.contains(download)) {
                                downloadsEntity.add(download)
                            }
                        }
                        Handler(Looper.getMainLooper()).post {
                            /**
                             * update counter
                             */
                            root!!.downloads_counter.text = downloadsEntity.size.toString()
                        }
                    }
                }.start()
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            root!!.iv_refresh.clearAnimation()
            super.onPageFinished(view, url)
        }
    }

    private fun populateBookmarks(){
        if (downloadsViewModel.countBookmark() == 0){
            downloadsViewModel.insertBookmark(BookmarkEntity(0, "Google", "https://www.google.com"))
            downloadsViewModel.insertBookmark(BookmarkEntity(0, "Facebook", "https://www.facebook.com"))
            downloadsViewModel.insertBookmark(BookmarkEntity(0, "Twitter", "https://www.twitter.com"))
            downloadsViewModel.insertBookmark(BookmarkEntity(0, "Daily Motion", "https://www.dailymotion.com"))
            downloadsViewModel.insertBookmark(BookmarkEntity(0, "Vimeo", "https://www.vimeo.com"))
            downloadsViewModel.insertBookmark(BookmarkEntity(0, "Tubidy", "https://www.tubidy.mobi"))
        }

        downloadsViewModel.getBookmark().observe(this, Observer<List<BookmarkEntity>>{ bookmarkEntities ->
            if (bookmarkEntities != null){
                if (bookmarkEntities.isNotEmpty()){
                    bookmarkEntity.clear()
                    for (d in 0 until bookmarkEntities.size){
                        val bookmark = BookmarkEntity(bookmarkEntities[d].id, bookmarkEntities[d].name, bookmarkEntities[d].url)
                        this.bookmarkEntity.add(bookmark)
                    }
                    bookmarkAdapter.setList(bookmarkEntity)
                }
            }
        })
    }

}