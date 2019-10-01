package com.download.manager.video.whatsapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.download.manager.video.whatsapp.BuildConfig
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.adapter.DownloadsAdapter
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.PermissionListener
import kotlinx.android.synthetic.main.activity_downloads.*
import java.io.File

class DownloadsActivity : AppCompatActivity(), DownloadsAdapter.OnItemClickListener {

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var downloadsEntity: MutableList<DownloadsEntity> = ArrayList()
    private lateinit var downloadsAdapter: DownloadsAdapter

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = "Downloads"
        toolbar.setNavigationOnClickListener { finish() }

        changeNavigationBar()

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

    private fun changeNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = resources.getColor(R.color.colorPrimary)
        }
    }

}