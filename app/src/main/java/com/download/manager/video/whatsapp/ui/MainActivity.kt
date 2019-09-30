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
import android.content.Context
import android.content.Intent
import android.support.annotation.RequiresApi
import com.download.manager.video.whatsapp.utility.service.ClipDataService
import com.download.manager.video.whatsapp.utility.service.InstaService

class MainActivity : AppCompatActivity() {

    private val menu by lazy { findViewById<ReadableBottomBar>(R.id.main_navigation) }

    lateinit var mFragmentManager: FragmentManager
    lateinit var mFragmentTransaction: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        PermissionListener(this).loadPermissions()

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

}
