package com.download.manager.video.whatsapp.ui.navigation

import android.annotation.SuppressLint
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import com.download.manager.video.whatsapp.ui.MainActivity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.adapter.InstaAdapter
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridLayoutManager
import kotlinx.android.synthetic.main.dialog_add_url.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import android.support.v4.content.FileProvider
import android.widget.TextView
import com.download.manager.video.whatsapp.BuildConfig
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.engine.AdPreferrenceHandler
import com.download.manager.video.whatsapp.engine.Constants
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.dialog_how_to_instagram.*
import kotlinx.android.synthetic.main.main_gram.*
import kotlinx.android.synthetic.main.main_gram.view.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat


class Instagram : Fragment(), InstaAdapter.OnItemClickListener  {

    /**
     * Ad related variables
     */
    private var root: View? = null
    private lateinit var mainIntrAd: InterstitialAd
    lateinit var adPreferrenceHandler: AdPreferrenceHandler

    private lateinit var dialog: Dialog

    lateinit var saveDialog: Dialog

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var instaEntity: MutableList<InstaEntity> = ArrayList()
    private lateinit var instaAdapter: InstaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(root!!.toolbar)
        }
        (activity as MainActivity).supportActionBar!!.title = "Home | Instagram"

        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)
        adPreferrenceHandler = AdPreferrenceHandler(activity as MainActivity)

        // Initialize the Mobile Ads SDK with an AdMob App ID.
        MobileAds.initialize(activity as MainActivity, resources.getString(R.string.appd_name))

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
        instaAdapter = InstaAdapter(activity as MainActivity, instaEntity)
        instaAdapter.setOnItemClickListener(this)
        val instaManager = StickyHeaderGridLayoutManager(2)
        instaManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))

        root!!.insta_history.layoutManager = instaManager
        root!!.insta_history.itemAnimator = DefaultItemAnimator()
        root!!.insta_history.adapter = instaAdapter

        root!!.insta_help.setOnClickListener {
            saveDialog = Dialog(activity)
            saveDialog.setCanceledOnTouchOutside(false)
            saveDialog.setCancelable(true)
            saveDialog.setContentView(R.layout.dialog_how_to_instagram)
            Objects.requireNonNull<Window>(saveDialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            saveDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            saveDialog.window!!.setGravity(Gravity.BOTTOM)
            saveDialog.show()

            val dismiss = saveDialog.dc_dismiss

            dismiss.setOnClickListener { saveDialog.dismiss() }
        }

        if (adPreferrenceHandler.getViewSessionCount() == 2){
            intrAdLoader()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.main_gram, container, false)  // initialize it here
        return root
    }

    /** Called when leaving the activity  */
    override fun onPause() {
        root!!.gram_view.pause()
        super.onPause()
    }

    /** Called when returning to the activity  */
    override fun onResume() {
        populateDownloads()
        root!!.gram_view.resume()
        super.onResume()
    }

    /** Called before the activity is destroyed  */
    override fun onDestroy() {
        root!!.gram_view.destroy()
        super.onDestroy()
    }

    private fun populateDownloads(){
        runBlocking { if (downloadsViewModel.countInstaList() == 0) { getDownloadData() } }
        downloadsViewModel.getInsta().observe(this, Observer<List<InstaEntity>>{ instaEntities ->
            if (instaEntities != null){
                if (instaEntities.isNotEmpty()){
                    root!!.insta_history.visibility = View.VISIBLE
                    root!!.insta_empty.visibility = View.GONE

                    instaEntity.clear()
                    for (d in 0 until instaEntities.size){
                        val insta = InstaEntity(
                            instaEntities[d].id, instaEntities[d].name, instaEntities[d].postedBy, instaEntities[d].parentUrl, instaEntities[d].liveUrl, instaEntities[d].localUrl, instaEntities[d].type, instaEntities[d].downloaded, instaEntities[d].size, instaEntities[d].datecreated
                        )
                        this.instaEntity.add(insta)
                    }
                    instaAdapter.setInsta(instaEntity)
                }else{
                    root!!.insta_history.visibility = View.GONE
                    root!!.insta_empty.visibility = View.VISIBLE
                }
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDownloadData() {
        val savedFiles = File(Environment.getExternalStorageDirectory().absolutePath + Constants().INSTAGRAM_FOLDER).listFiles()

        if (savedFiles != null) {
            for (s in savedFiles.indices) {
                val type = if (Uri.fromFile(savedFiles[s]).toString().endsWith(".mp4")) { "Video" } else { "Image" }
                val insta = InstaEntity(0, savedFiles[s].name, "adm", savedFiles[s].absolutePath, savedFiles[s].absolutePath, savedFiles[s].absolutePath, type, savedFiles[s].length().toString(), savedFiles[s].length().toString(), SimpleDateFormat("dd-MM-yyyy").format(Date(savedFiles[s].lastModified())))

                if (downloadsViewModel.countWhatsListByName(Uri.fromFile(savedFiles[s]).toString()) == 0) {
                    this.instaEntity.add(insta)
                    downloadsViewModel.insertInsta(insta)
                }
            }
        }
    }

    override fun parentClick(localUrl: String, type: String) {
        adCountHandler()
        val videoFile = File(localUrl)
        val fileUri = FileProvider.getUriForFile(context!!, BuildConfig.APPLICATION_ID +".admprovider", videoFile)
        (activity as MainActivity).grantUriPermission("com.download.manager.video.whatsapp", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent(Intent.ACTION_VIEW)
        if (type.equals("Image", true)){
            intent.setDataAndType(fileUri, "image/*")
        }else{
            intent.setDataAndType(fileUri, "video/*")
        }
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//DO NOT FORGET THIS EVER
        startActivity(intent)
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

    fun instaBannerAdLoader(){
        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        root!!.gram_view.loadAd(adRequest)
        // Toast.makeText(activity as MainActivity, "Load insta banner ad", Toast.LENGTH_SHORT).show()
    }

    fun pauseInstaBannerAdLoader(){
        // Start loading the ad in the background.
        root!!.gram_view.pause()
        // Toast.makeText(activity as MainActivity, "Insta Banner pause", Toast.LENGTH_SHORT).show()
    }

    fun resumeInstaBannerAdLoader(){
        // Start loading the ad in the background.
        root!!.gram_view.resume()
        // Toast.makeText(activity as MainActivity, "Insta Banner resume", Toast.LENGTH_SHORT).show()
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