package com.download.manager.video.whatsapp.ui.navigation

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.adapter.WhatsAdapter
import com.download.manager.video.whatsapp.database.entity.WhatsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.Constants
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.ui.MainActivity
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridLayoutManager
import kotlinx.android.synthetic.main.main_whatsapp.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList



class Whatsapp : Fragment(), WhatsAdapter.OnItemClickListener {

    override fun parentClick(view: View, position: Int, userCode: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

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
        (activity as MainActivity).supportActionBar!!.title = "Home | Whatsapp"

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        /**
         * Initializing adapter and layout manager for recyclerView
         */
        whatsAdapter = WhatsAdapter(activity as MainActivity, whatsEntity)
        whatsAdapter.setOnItemClickListener(this)
        val whatsManager = StickyHeaderGridLayoutManager(3)
        whatsManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))

        whats_history.layoutManager = whatsManager
        whats_history.itemAnimator = DefaultItemAnimator()
        whats_history.adapter = whatsAdapter

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedWhatsnceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_whatsapp, container, false)
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({ populateDownloads() }, 200)
    }

    private fun populateDownloads(){
        runBlocking { getDownloadData() }
        downloadsViewModel.getWhats().observe(this, Observer<List<WhatsEntity>>{ whatsEntities ->
            if (whatsEntities != null){
                if (whatsEntities.isNotEmpty()){
                    whats_history.visibility = View.VISIBLE
                    whats_empty.visibility = View.GONE

                    whatsEntity.clear()
                    for (d in 0 until whatsEntities.size){
                        val whats = WhatsEntity(whatsEntities[d].id, whatsEntities[d].name, whatsEntities[d].liveUrl, whatsEntities[d].liveUrl, whatsEntities[d].localUrl,
                            whatsEntities[d].status, whatsEntities[d].type, whatsEntities[d].size, whatsEntities[d].timestamp, whatsEntities[d].datecreated)
                        this.whatsEntity.add(whats)
                    }
                    whatsAdapter.setWhats(whatsEntity)
                }else{
                    whats_history.visibility = View.GONE
                    whats_empty.visibility = View.VISIBLE
                }
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDownloadData() {
        val files = File(Environment.getExternalStorageDirectory().absolutePath + Constants().FOLDER_NAME).listFiles()
        // val savedFiles = File(Environment.getExternalStorageDirectory().absolutePath + Constants().DOWNLOADER_FOLDER).listFiles()
        Log.e("files count live", files.indices.toString())
        for (i in files.indices) {
            val type = if (Uri.fromFile(files[i]).toString().endsWith(".mp4")) { "Video" } else { "Image" }

            val whats = WhatsEntity(0, files[i].name, files[i].absolutePath, Uri.fromFile(files[i]).toString(), "", "live", type, files[i].length().toString(), SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date(files[i].lastModified())), Legion().getCurrentDate())
            if (downloadsViewModel.countWhatsListByName(files[i].name) == 0){
                Log.e("files count names", files[i].name)
                this.whatsEntity.add(whats)
                downloadsViewModel.insertWhats(whats)
            }
        }

    }

}