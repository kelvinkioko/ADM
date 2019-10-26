package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.download.manager.video.whatsapp.BuildConfig
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.adapter.DownloadsAdapter
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.AdPreferrenceHandler
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.ui.MainActivity
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.main_downloads.*
import kotlinx.android.synthetic.main.main_downloads.view.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class Downloader : Fragment(), DownloadsAdapter.OnItemClickListener {

    /**
     * Ad related variables
     */
    private var root: View? = null
    private lateinit var mainIntrAd: InterstitialAd
    lateinit var adPreferrenceHandler: AdPreferrenceHandler

    private lateinit var dialog: Dialog

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var downloadsEntity: MutableList<DownloadsEntity> = ArrayList()
    private lateinit var downloadsAdapter: DownloadsAdapter

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.main_downloads, container, false)  // initialize it here
        return root
    }

    override fun parentClick(localUrl: String) {
        val videoFile = File(localUrl)
        val fileUri = FileProvider.getUriForFile(context!!, BuildConfig.APPLICATION_ID +".admprovider", videoFile)
        (activity as MainActivity).grantUriPermission("com.download.manager.video.whatsapp", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent(Intent.ACTION_VIEW)
        when {
            localUrl.endsWith(".mp4", true) -> intent.setDataAndType(fileUri, "video/*")
            localUrl.endsWith(".jpg", true) -> intent.setDataAndType(fileUri, "image/*")
            localUrl.endsWith(".jpeg", true) -> intent.setDataAndType(fileUri, "image/*")
            localUrl.endsWith(".mp3", true) -> intent.setDataAndType(fileUri, "audio/*")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//DO NOT FORGET THIS EVER
        //DO NOT FORGET THIS EVER
        startActivity(intent)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }
        (activity as MainActivity).supportActionBar!!.title = "Downloads"

        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(activity as MainActivity)
        adPreferrenceHandler = AdPreferrenceHandler(activity as MainActivity)

        // Initialize the Mobile Ads SDK with an AdMob App ID.
        MobileAds.initialize(activity as MainActivity)

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        root!!.download_ad_view.loadAd(adRequest)

        // Create the InterstitialAd and set it up.
        mainIntrAd = InterstitialAd(activity as MainActivity).apply {
            adUnitId = resources.getString(R.string.intr_name)
            adListener = (object : AdListener() {
                override fun onAdLoaded() {
                    if (adPreferrenceHandler.getViewSessionCount() >= 5) {
                        showInterstitial()
                        adPreferrenceHandler.setViewSessionCount(0)
                    }else{
                        adPreferrenceHandler.setViewSessionCount(adPreferrenceHandler.getViewSessionCount() + 1)
                    }
                }
                override fun onAdFailedToLoad(errorCode: Int) {}
                override fun onAdClosed() {}
            })
        }

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        /**
         * Initializing adapter and layout manager for recyclerView
         */
        downloadsAdapter = DownloadsAdapter(activity as MainActivity, downloadsEntity)
        downloadsAdapter.setOnItemClickListener(this)
        val whatsManager = LinearLayoutManager(activity as MainActivity, LinearLayoutManager.VERTICAL, false)
        download_history.layoutManager = whatsManager
        download_history.itemAnimator = DefaultItemAnimator()
        download_history.adapter = downloadsAdapter

    }

    /** Called when leaving the activity  */
    override fun onPause() {
        root!!.download_ad_view.pause()
        super.onPause()
    }

    /** Called when returning to the activity  */
    override fun onResume() {
        populateDownloads()
        root!!.download_ad_view.resume()
        super.onResume()
    }

    /** Called before the activity is destroyed  */
    override fun onDestroy() {
        root!!.download_ad_view.destroy()
        super.onDestroy()
    }

    private fun populateDownloads(){
        downloadsViewModel.getDownloads().observe(this, Observer<List<DownloadsEntity>>{ downloadsEntities ->
            if (downloadsEntities != null){
                if (downloadsEntities.isNotEmpty()){
                    download_history.visibility = View.VISIBLE
                    download_empty.visibility = View.GONE

                    downloadsEntity.clear()
                    for (d in 0 until downloadsEntities.size){
                        val download = DownloadsEntity(downloadsEntities[d].id, downloadsEntities[d].name, downloadsEntities[d].url, downloadsEntities[d].localurl,
                            downloadsEntities[d].downloaded, downloadsEntities[d].size, downloadsEntities[d].datecreated)
                        this.downloadsEntity.add(download)
                    }

                    downloadsAdapter.setDownloads(downloadsEntity)
                }else{
                    download_history.visibility = View.GONE
                    download_empty.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun showInterstitial() {
        if (mainIntrAd.isLoaded) {
            mainIntrAd.show()
        }else{
            if (!mainIntrAd.isLoading && !mainIntrAd.isLoaded) {
                intrAdLoader()
            }
        }
    }

    private fun intrAdLoader(){
        // Create an ad request.
        val adRequestIntr = AdRequest.Builder().build()
        mainIntrAd.loadAd(adRequestIntr)
    }

    private fun adCountHandler(){
        if (adPreferrenceHandler.getViewSessionCount() >= 5) {
            showInterstitial()
            adPreferrenceHandler.setViewSessionCount(0)
        }else{
            adPreferrenceHandler.setViewSessionCount(adPreferrenceHandler.getViewSessionCount() + 1)
        }
    }

    //inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_rate -> {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App rated")
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + (activity as MainActivity).packageName)))
            true
        }
        R.id.action_share ->{
            reviewDialog()
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun reviewDialog(){
        dialog = Dialog(activity as MainActivity)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_review_rating)
        Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.show()

        val _share : TextView = dialog.findViewById(R.id.drr_share)
        val _rate : TextView = dialog.findViewById(R.id.drr_rate)
        val googlePlayUrl = "https://play.google.com/store/apps/details?id="
        val msg = resources.getString(R.string.share_message) + " "

        _share.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App shared")
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg + googlePlayUrl + (activity as MainActivity).packageName)
            shareIntent.type = "text/plain"
            startActivity(Intent.createChooser(shareIntent, "Share..."))
        }

        _rate.setOnClickListener{
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App rated")
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + (activity as MainActivity).packageName)))
        }
    }

}