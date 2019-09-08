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
        whatsEntity.clear()
        runBlocking { getDownloadData() }
        if (whatsEntity.size > 0) {
            whats_history.visibility = View.VISIBLE
            whats_empty.visibility = View.GONE
            whatsAdapter.setWhats(whatsEntity)
        } else {
            whats_history.visibility = View.GONE
            whats_empty.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDownloadData() {
        val files = File(Environment.getExternalStorageDirectory().absolutePath + Constants().FOLDER_NAME).listFiles()
        val savedFiles = File(Environment.getExternalStorageDirectory().absolutePath + Constants().DOWNLOADER_FOLDER).listFiles()
        var names = ""
        try {
            for (i in savedFiles.indices) {
                if (names.isEmpty()){ names = savedFiles[i].name } else { names += ",$savedFiles[i].name" }
                val type = if (Uri.fromFile(savedFiles[i]).toString().endsWith(".mp4")) { "Video" } else { "Image" }

                val whats = WhatsEntity(0, savedFiles[i].name, savedFiles[i].absolutePath, Uri.fromFile(savedFiles[i]).toString(),"downloaded", type, savedFiles[i].length().toString(), SimpleDateFormat("dd-MM-yyyy").format(Date(savedFiles[i].lastModified())))
                this.whatsEntity.add(whats)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        for (i in files.indices) {
            if (!names.contains(files[i].name)) {
                val type = if (Uri.fromFile(files[i]).toString().endsWith(".mp4")) { "Video" } else { "Image" }

                val whats = WhatsEntity(0, files[i].name, files[i].absolutePath, Uri.fromFile(files[i]).toString(), "live", type, files[i].length().toString(), SimpleDateFormat("dd-MM-yyyy").format(Date(files[i].lastModified())))
                this.whatsEntity.add(whats)
            }
        }

    }

}