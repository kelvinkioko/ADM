package com.download.manager.video.whatsapp.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.adapter.DownloadsAdapter
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.PermissionListener
import kotlinx.android.synthetic.main.activity_downloads.*

class DownloadsActivity : AppCompatActivity(), DownloadsAdapter.OnItemClickListener {

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var downloadsEntity: MutableList<DownloadsEntity> = ArrayList()
    private lateinit var downloadsAdapter: DownloadsAdapter

    override fun parentClick(view: View, position: Int, userCode: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloads)
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