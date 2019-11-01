package com.download.manager.video.whatsapp.ui.navigation

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.download.manager.video.whatsapp.BuildConfig
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.adapter.WhatsAdapter
import com.download.manager.video.whatsapp.database.entity.WhatsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.AdPreferrenceHandler
import com.download.manager.video.whatsapp.engine.Constants
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.ui.MainActivity
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridLayoutManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.main_whatsapp.*
import kotlinx.android.synthetic.main.main_whatsapp.toolbar
import kotlinx.android.synthetic.main.main_whatsapp.view.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Whatsapp : Fragment(), WhatsAdapter.OnItemClickListener {

    /**
     * Ad related variables
     */
    private var root: View? = null
    private lateinit var mainIntrAd: InterstitialAd
    lateinit var adPreferrenceHandler: AdPreferrenceHandler

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var whatsEntity: MutableList<WhatsEntity> = ArrayList()
    private lateinit var whatsAdapter: WhatsAdapter
    private val handler: Handler = Handler()

    override fun onCreate(savedWhatsnceState: Bundle?) {
        super.onCreate(savedWhatsnceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedWhatsnceState: Bundle?) {
        super.onActivityCreated(savedWhatsnceState)
        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(root!!.toolbar)
        }
        (activity as MainActivity).supportActionBar!!.title = "Home | Whatsapp"

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)
        adPreferrenceHandler = AdPreferrenceHandler(activity as MainActivity)

        // Initialize the Mobile Ads SDK with an AdMob App ID.
        MobileAds.initialize(activity as MainActivity, resources.getString(R.string.appd_name))

        whatsBannerAdLoader()

        // Create the InterstitialAd and set it up.
        mainIntrAd = InterstitialAd(activity as MainActivity).apply {
            adUnitId = resources.getString(R.string.intr_name)
            adListener = (object : AdListener() {
                override fun onAdLoaded() {
                    if (adPreferrenceHandler.getViewSessionCount() >= 3) {
                        showInterstitial()
                        adPreferrenceHandler.setViewSessionCount(0)
                    }else{
                        adPreferrenceHandler.setViewSessionCount(adPreferrenceHandler.getViewSessionCount() + 1)
                        if (adPreferrenceHandler.getViewSessionCount() == 2){ intrAdLoader() }
                    }
                }
                override fun onAdFailedToLoad(errorCode: Int) {}
                override fun onAdClosed() {}
            })
        }

        /**
         * Initializing adapter and layout manager for recyclerView
         */
        whatsAdapter = WhatsAdapter(activity as MainActivity, whatsEntity)
        whatsAdapter.setOnItemClickListener(this)
        val whatsManager = StickyHeaderGridLayoutManager(2)
        whatsManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))

        root!!.whats_history.layoutManager = whatsManager
        root!!.whats_history.itemAnimator = DefaultItemAnimator()
        root!!.whats_history.adapter = whatsAdapter

        if (adPreferrenceHandler.getViewSessionCount() == 2){ intrAdLoader() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedWhatsnceState: Bundle?): View? {
        root = inflater.inflate(R.layout.main_whatsapp, container, false)  // initialize it here
        return root
    }

    /** Called when leaving the activity  */
    override fun onPause() {
        root!!.whatsapp_view.pause()
        super.onPause()
    }

    /** Called when returning to the activity  */
    override fun onResume() {
        Handler().postDelayed({ populateDownloads() }, 200)
        root!!.whatsapp_view.resume()
        super.onResume()
    }

    /** Called before the activity is destroyed  */
    override fun onDestroy() {
        root!!.whatsapp_view.destroy()
        super.onDestroy()
    }

    private fun populateDownloads(){
        runBlocking { getDownloadData() }
        downloadsViewModel.getWhats().observe(this, Observer<List<WhatsEntity>>{ whatsEntities ->
            if (whatsEntities != null){
                if (whatsEntities.isNotEmpty()){
                    root!!.whats_history.visibility = View.VISIBLE
                    root!!.whats_empty.visibility = View.GONE
                    whatsEntity.clear()
                    for (d in 0 until whatsEntities.size){
                        val whats = WhatsEntity(whatsEntities[d].id, whatsEntities[d].name, whatsEntities[d].liveUrl, whatsEntities[d].liveUrl, whatsEntities[d].localUrl,
                            whatsEntities[d].status, whatsEntities[d].type, whatsEntities[d].size, whatsEntities[d].timestamp, whatsEntities[d].datecreated)
                        this.whatsEntity.add(whats)
                    }
                    whatsAdapter.setWhats(whatsEntity)
                }else{
                    root!!.whats_history.visibility = View.GONE
                    root!!.whats_empty.visibility = View.VISIBLE
                }
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDownloadData() {
        val files = File(Environment.getExternalStorageDirectory().absolutePath + Constants().FOLDER_NAME).listFiles()
        val savedFiles = File(Environment.getExternalStorageDirectory().absolutePath + Constants().DOWNLOADER_FOLDER).listFiles()

        if (savedFiles != null) {
            for (s in savedFiles.indices) {
                val type = if (Uri.fromFile(savedFiles[s]).toString().endsWith(".mp4")) { "Video" } else { "Image" }
                val whats = WhatsEntity(0, savedFiles[s].name, savedFiles[s].absolutePath, Uri.fromFile(savedFiles[s]).toString(), savedFiles[s].absolutePath, "downloaded", type, savedFiles[s].length().toString(), savedFiles[s].lastModified().toString(), SimpleDateFormat("dd-MM-yyyy").format(Date(savedFiles[s].lastModified())))

                if (downloadsViewModel.countWhatsListByName(Uri.fromFile(savedFiles[s]).toString()) == 0) {
                    this.whatsEntity.add(whats)
                    downloadsViewModel.insertWhats(whats)
                }
            }
        }

        if (files != null) {
            for (i in files.indices) {
                val type = if (Uri.fromFile(files[i]).toString().endsWith(".mp4")) { "Video" } else { "Image" }
                val whats = WhatsEntity(0, files[i].name, files[i].absolutePath, Uri.fromFile(files[i]).toString(), "", "live", type, files[i].length().toString(), files[i].lastModified().toString(), SimpleDateFormat("dd-MM-yyyy").format(Date(files[i].lastModified())))

                if (downloadsViewModel.countWhatsListByName(Uri.fromFile(files[i]).toString()) == 0) {
                    this.whatsEntity.add(whats)
                    downloadsViewModel.insertWhats(whats)
                }

                if (downloadsViewModel.countWhatsListByNameAndDownloaded(Uri.fromFile(files[i]).toString()) == 1) {
                    downloadsViewModel.deleteWhatsListByNameAndDownloaded(Uri.fromFile(files[i]).toString())
                }
            }
        }
    }

    override fun parentClick(localUrl: String) {
        adCountHandler()
        val videoFile = File(localUrl)
        val fileUri = FileProvider.getUriForFile(context!!, BuildConfig.APPLICATION_ID +".admprovider", videoFile)
        (activity as MainActivity).grantUriPermission("com.download.manager.video.whatsapp", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent(Intent.ACTION_VIEW)
        if (localUrl.endsWith(".jpg")){
            intent.setDataAndType(fileUri, "image/*")
        }else{
            intent.setDataAndType(fileUri, "video/*")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//DO NOT FORGET THIS EVER
        startActivity(intent)
    }

    private fun showInterstitial() {
        intrAdLoader()
        if (mainIntrAd.isLoaded) {
            mainIntrAd.show()
        }else{
            if (!mainIntrAd.isLoading && !mainIntrAd.isLoaded) {
                intrAdLoader()
            }
        }
    }

    fun whatsBannerAdLoader(){
        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        root!!.whatsapp_view.loadAd(adRequest)
        // Toast.makeText(activity as MainActivity, "Load whatapp Banner Ad", Toast.LENGTH_SHORT).show()
    }

    fun pauseBannerAdLoader(){
        // Start loading the ad in the background.
        root!!.whatsapp_view.pause()
        // Toast.makeText(activity as MainActivity, "whatsapp Banner pause", Toast.LENGTH_SHORT).show()
    }

    fun resumeBannerAdLoader(){
        // Start loading the ad in the background.
        root!!.whatsapp_view.resume()
        // Toast.makeText(activity as MainActivity, "whatsapp Banner resume", Toast.LENGTH_SHORT).show()
    }

    private fun intrAdLoader(){
        // Create an ad request.
        val adRequestIntr = AdRequest.Builder().build()
        mainIntrAd.loadAd(adRequestIntr)
    }

    private fun adCountHandler(){
        if (adPreferrenceHandler.getViewSessionCount() >= 3) {
            showInterstitial()
            adPreferrenceHandler.setViewSessionCount(0)
        }else{
            adPreferrenceHandler.setViewSessionCount(adPreferrenceHandler.getViewSessionCount() + 1)
            if (adPreferrenceHandler.getViewSessionCount() == 2){ intrAdLoader() }
        }
    }

}