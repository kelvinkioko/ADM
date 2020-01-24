package com.download.manager.video.whatsapp.ui

import android.app.Dialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.app.AppCompatDelegate
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.ui.navigation.Browser
import com.download.manager.video.whatsapp.widgets.ReadableBottomBar
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.download.manager.video.whatsapp.engine.AdPreferrenceHandler
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.ui.navigation.Downloader
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import java.util.*

class MainActivity : AppCompatActivity()  {

    /**
     * DownloaderView actions
     */
    private lateinit var mainIntrAd: InterstitialAd

    private lateinit var dialog: Dialog

    private val menu by lazy { findViewById<ReadableBottomBar>(R.id.main_navigation) }

    lateinit var mFragmentManager: FragmentManager
    lateinit var mFragmentTransaction: FragmentTransaction
    var count = 0

    lateinit var adPreferrenceHandler: AdPreferrenceHandler

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        PermissionListener(this).loadPermissions()
        adPreferrenceHandler = AdPreferrenceHandler(this@MainActivity)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this@MainActivity)

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App opened")
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
            })

        // Initialize the Mobile Ads SDK with an AdMob App ID.
        MobileAds.initialize(this, resources.getString(R.string.appd_name))

        // Create the InterstitialAd and set it up.
        mainIntrAd = InterstitialAd(this).apply {
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

        if (adPreferrenceHandler.getFirstOpenCount()){
            reviewDialog()
            adPreferrenceHandler.setFirstOpenCount(false)
        }

        reviewRequestHandler()

        // Get the text fragment instance
        val browserFragment = Browser()
        val downloaderFragment = Downloader()

        // Get the support fragment manager instance
        mFragmentManager = supportFragmentManager
        // Begin the fragment transition using support fragment manager
        mFragmentTransaction = mFragmentManager.beginTransaction()
        mFragmentTransaction.add(R.id.download_container, browserFragment)
            .add(R.id.download_container, downloaderFragment)
            .hide(downloaderFragment)
            .commit()

        menu.selectItem(0)

        menu.setOnItemSelectListener( object :ReadableBottomBar.ItemSelectListener{
            override fun onItemSelected(index: Int) {
                when (index) {
                    0 -> {
                        adCountHandler()

                        // Begin the fragment transition using support fragment manager
                        mFragmentTransaction = mFragmentManager.beginTransaction()
                        mFragmentTransaction.show(browserFragment).hide(downloaderFragment).commit()

                        /* ************************** *\
                            Ad Management
                        \* ************************** */
                        downloaderFragment.pauseDownloadBannerAdLoader()

                        val browserBundle = Bundle()
                        browserBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Browser tab opened")
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Param.LOCATION, browserBundle)
                    }
                    else -> {
                        adCountHandler()

                        // Begin the fragment transition using support fragment manager
                        mFragmentTransaction = mFragmentManager.beginTransaction()
                        mFragmentTransaction.hide(browserFragment).show(downloaderFragment).commit()
                        downloaderFragment.populateDownloads()

                        /* ************************** *\
                            Ad Management
                        \* ************************** */
                        downloaderFragment.downloadBannerAdLoader()
                        downloaderFragment.resumeDownloadBannerAdLoader()

                        val downloadsBundle = Bundle()
                        downloadsBundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Downloads tab opened")
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Param.LOCATION, downloadsBundle)
                    }
                }
            }
        })

        changeNavigationBar()
    }

    private fun changeNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = resources.getColor(R.color.colorPrimary)
        }
    }

    override fun onBackPressed() {
        if(count == 1) {
            count = 0
            moveTaskToBack(false)
        } else {
            reviewRequestHandler()
            Toast.makeText(this, "Press Back again to quit.", Toast.LENGTH_SHORT).show()
            count++
        }
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
        if (adPreferrenceHandler.getTabViewSessionCount() >= 4) {
            showInterstitial()
            adPreferrenceHandler.setTabViewSessionCount(0)
        }else{
            adPreferrenceHandler.setTabViewSessionCount(adPreferrenceHandler.getTabViewSessionCount() + 1)
            if (adPreferrenceHandler.getTabViewSessionCount() == 3){ intrAdLoader() }
        }
    }

    //setting menu in action bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    // actions on click menu items
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_rate -> {
            val rateBundle = Bundle()
            rateBundle.putString(FirebaseAnalytics.Param.LOCATION, "App rated")
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Param.LOCATION, rateBundle)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
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

    fun reviewDialog(){
        dialog = Dialog(this@MainActivity)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_review_rating)
        Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.show()

        val _dismiss : TextView = dialog.findViewById(R.id.drr_dismiss)
        val _share : TextView = dialog.findViewById(R.id.drr_share)
        val _rate : TextView = dialog.findViewById(R.id.drr_rate)
        val _message : TextView = dialog.findViewById(R.id.drr_message)
        val googlePlayUrl = "https://play.google.com/store/apps/details?id="
        _message.text = if (adPreferrenceHandler.getFirstOpenCount()){
            resources.getString(R.string.first_message) + " "
        }else{
            resources.getString(R.string.review_message) + " "
        }
        val msg = resources.getString(R.string.share_message) + " "

        _share.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App shared")
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg + googlePlayUrl + packageName)
            shareIntent.type = "text/plain"
            startActivity(Intent.createChooser(shareIntent, "Share..."))

            adPreferrenceHandler.setWeeklyReviewSessionCount(Legion().getCurrentDate())
            dialog.dismiss()
        }

        _rate.setOnClickListener{
            val rateBundle = Bundle()
            rateBundle.putString(FirebaseAnalytics.Param.LOCATION, "App rated")
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Param.LOCATION, rateBundle)
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))

            adPreferrenceHandler.setWeeklyReviewSessionCount(Legion().getCurrentDate())
            dialog.dismiss()
        }

        _dismiss.setOnClickListener{
            adPreferrenceHandler.setWeeklyReviewSessionCount(Legion().getCurrentDate())
            dialog.dismiss()
        }
    }

    /** Called when returning to the activity  */
    override fun onResume() {
        reviewRequestHandler()
        super.onResume()
    }

    private fun reviewRequestHandler(){
        if(adPreferrenceHandler.getWeeklyReviewSessionCount().isNullOrEmpty() ||
            adPreferrenceHandler.getWeeklyReviewSessionCount().equals("none")){
            adPreferrenceHandler.setWeeklyReviewSessionCount(Legion().getCurrentDate())
        }else if (Legion().getWeeklyReviewRequestDays(adPreferrenceHandler.getWeeklyReviewSessionCount().toString()) >= 5) {
            reviewDialog()
        }
    }

}
