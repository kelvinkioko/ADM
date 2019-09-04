package com.download.manager.video.whatsapp.ui

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatDelegate
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.ui.navigation.Downloads
import com.download.manager.video.whatsapp.ui.navigation.Instagram
import com.download.manager.video.whatsapp.ui.navigation.Whatsapp
import com.download.manager.video.whatsapp.widgets.ReadableBottomBar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val menu by lazy { findViewById<ReadableBottomBar>(R.id.main_navigation) }

    lateinit var mFragmentManager: FragmentManager
    lateinit var mFragmentTransaction: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setSupportActionBar(toolbar)

        // Get the text fragment instance
        val tripFragment = Downloads()
        // Get the support fragment manager instance
        mFragmentManager = supportFragmentManager
        // Begin the fragment transition using support fragment manager
        mFragmentTransaction = mFragmentManager.beginTransaction()
        // Replace the fragment on container
        mFragmentTransaction.replace(R.id.download_container, tripFragment).commit()

        menu.selectItem(0)

        menu.setOnItemSelectListener( object :ReadableBottomBar.ItemSelectListener{
            override fun onItemSelected(index: Int) {
                when (index) {
                    0 -> {
                        // Get the text fragment instance
                        val downloadFragment = Downloads()
                        // Get the support fragment manager instance
                        mFragmentManager = supportFragmentManager
                        // Begin the fragment transition using support fragment manager
                        mFragmentTransaction = mFragmentManager.beginTransaction()
                        // Replace the fragment on container
                        mFragmentTransaction.replace(R.id.download_container, downloadFragment).commit()
                    }
                    1 -> {
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
                    else -> {
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