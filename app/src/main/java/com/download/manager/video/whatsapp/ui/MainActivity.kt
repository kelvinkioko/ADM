package com.download.manager.video.whatsapp.ui

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatDelegate
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.ui.navigation.Browser
import com.download.manager.video.whatsapp.ui.navigation.Instagram
import com.download.manager.video.whatsapp.ui.navigation.Whatsapp
import com.download.manager.video.whatsapp.widgets.ReadableBottomBar
import android.app.job.JobInfo
import android.content.ComponentName
import android.app.job.JobScheduler
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.support.v4.content.FileProvider
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.download.manager.video.whatsapp.BuildConfig
import com.download.manager.video.whatsapp.database.adapter.DownloadsAdapter
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.utility.service.ClipDataService
import com.download.manager.video.whatsapp.utility.service.InstaService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.download_empty
import kotlinx.android.synthetic.main.activity_main.download_history
import kotlinx.android.synthetic.main.activity_main.toolbar
import java.io.File

class MainActivity : AppCompatActivity(), DownloadsAdapter.OnItemClickListener  {

    /**
     * DownloaderView actions
     */

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var downloadsEntity: MutableList<DownloadsEntity> = ArrayList()
    private lateinit var downloadsAdapter: DownloadsAdapter

    private val menu by lazy { findViewById<ReadableBottomBar>(R.id.main_navigation) }

    lateinit var onBackPress: OnBackPressedListener
    lateinit var mFragmentManager: FragmentManager
    lateinit var mFragmentTransaction: FragmentTransaction
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = "Downloads"
        toolbar.setNavigationOnClickListener {
            main_page.visibility = View.VISIBLE
            downloads_page.visibility = View.GONE
        }

        PermissionListener(this).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        /**
         * Initializing adapter and layout manager for recyclerView
         */
        downloadsAdapter = DownloadsAdapter(this, downloadsEntity)
        downloadsAdapter.setOnItemClickListener(this)
        val whatsManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        download_history.layoutManager = whatsManager
        download_history.itemAnimator = DefaultItemAnimator()
        download_history.adapter = downloadsAdapter

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val jobScheduler = applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            val componentName = ComponentName(this, ClipDataService::class.java!!)

            val jobInfo = JobInfo.Builder(1, componentName)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setPeriodic(1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true).build()
            jobScheduler.schedule(jobInfo)
        }else {
            startService(Intent(this, InstaService::class.java).setAction(InstaService().ACTION_START))
        }

        // Get the text fragment instance
        val whatsappFragment = Whatsapp()
        // Get the support fragment manager instance
        mFragmentManager = supportFragmentManager
        // Begin the fragment transition using support fragment manager
        mFragmentTransaction = mFragmentManager.beginTransaction()
        // Animate the transitions as they happen
        // mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
        // Replace the fragment on container and finish the transition
        mFragmentTransaction.replace(R.id.download_container, whatsappFragment).commit()

        menu.selectItem(0)

        menu.setOnItemSelectListener( object :ReadableBottomBar.ItemSelectListener{
            override fun onItemSelected(index: Int) {
                when (index) {
                    0 -> {
                        // Get the text fragment instance
                        val whatsappFragment = Whatsapp()
                        // Get the support fragment manager instance
                        mFragmentManager = supportFragmentManager
                        // Begin the fragment transition using support fragment manager
                        mFragmentTransaction = mFragmentManager.beginTransaction()
                        // Animate the transitions as they happen
                        // mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        // Replace the fragment on container and finish the transition
                        mFragmentTransaction.replace(R.id.download_container, whatsappFragment).commit()
                    }
                    1 -> {
                        // Get the text fragment instance
                        val instagramFragment = Instagram()
                        // Get the support fragment manager instance
                        mFragmentManager = supportFragmentManager
                        // Begin the fragment transition using support fragment manager
                        mFragmentTransaction = mFragmentManager.beginTransaction()
                        // Animate the transitions as they happen
                        // mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        // Replace the fragment on container and finish the transition
                        mFragmentTransaction.replace(R.id.download_container, instagramFragment).commit()
                    }
                    else -> {
                        // Get the text fragment instance
                        val tripFragment = Browser()
                        // Get the support fragment manager instance
                        mFragmentManager = supportFragmentManager
                        // Begin the fragment transition using support fragment manager
                        mFragmentTransaction = mFragmentManager.beginTransaction()
                        // Animate the transitions as they happen
//                         mFragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        // Replace the fragment on container
                        mFragmentTransaction.replace(R.id.download_container, tripFragment).commit()
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
        if (downloads_page.visibility == View.VISIBLE){
            main_page.visibility = View.VISIBLE
            downloads_page.visibility = View.GONE
        }else {
            if(count == 1) {
                count = 0
                super.onBackPressed()
                finish()
            } else {
                Toast.makeText(this, "Press Back again to quit.", Toast.LENGTH_SHORT).show()
                count++
            }
//            val fragment = supportFragmentManager.findFragmentById(R.id.download_container)
//            if (fragment !is OnBackPressedListener || !(fragment as OnBackPressedListener).onBackPressed()) {
//                super.onBackPressed()
//            }
//
//            super.onBackPressed()
        }
    }

    interface OnBackPressedListener {
        fun onBackPressed(): Boolean
    }

    /**
     * DownloaderView actions
     */

    override fun parentClick(localUrl: String) {
        Log.e("Click notifier", localUrl)
        val videoFile = File(localUrl)
        val fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID +".admprovider", videoFile)
        grantUriPermission("com.download.manager.video.whatsapp", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent(Intent.ACTION_VIEW)
        if (localUrl.endsWith(".mp4", true)){
            intent.setDataAndType(fileUri, "video/*")
        }else if (localUrl.endsWith(".jpg", true)){
            intent.setDataAndType(fileUri, "image/*")
        }else if (localUrl.endsWith(".jpeg", true)){
            intent.setDataAndType(fileUri, "image/*")
        }else if (localUrl.endsWith(".mp3", true)){
            intent.setDataAndType(fileUri, "audio/*")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//DO NOT FORGET THIS EVER
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({ populateDownloads() }, 200)
    }

    private fun populateDownloads(){
        downloadsViewModel.getDownloads().observe(this, Observer<List<DownloadsEntity>>{ downloadsEntities ->
            if (downloadsEntities != null){
                if (downloadsEntities.isNotEmpty()){
                    download_history.visibility = View.VISIBLE
                    download_empty.visibility = View.GONE
                    Log.e("files count observer", downloadsEntities.size.toString())
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

}
