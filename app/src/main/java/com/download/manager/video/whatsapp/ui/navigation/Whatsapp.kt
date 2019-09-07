package com.download.manager.video.whatsapp.ui.navigation

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

//        populateDownloads()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedWhatsnceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_whatsapp, container, false)
    }

    override fun onResume() {
        super.onResume()
        Handler().postDelayed({ populateDownloads() }, 200)
    }

    private fun populateDownloads(){
        downloadsViewModel.getWhats().observe(this, Observer<List<WhatsEntity>>{ whatsEntities ->
            if (whatsEntities != null){
                if (whatsEntities.isNotEmpty()){
                    whats_history.visibility = View.VISIBLE
                    whats_empty.visibility = View.GONE

                    whatsEntity.clear()
                    runBlocking { getSavedData() }
                    for (d in 0 until whatsEntities.size){
                        val whats = WhatsEntity(whatsEntities[d].id, whatsEntities[d].name, whatsEntities[d].liveUrl, whatsEntities[d].liveUri, whatsEntities[d].localUrl, whatsEntities[d].type, whatsEntities[d].size, whatsEntities[d].datecreated)
                        this.whatsEntity.add(whats)
                    }
                    whatsAdapter.setWhats(whatsEntity)
                }else{
                    whatsEntity.clear()
                    runBlocking { getSavedData() }
                    if (whatsEntity.size > 0) {
                        whats_history.visibility = View.VISIBLE
                        whats_empty.visibility = View.GONE
                        whatsAdapter.setWhats(whatsEntity)
                    } else {
                        whats_history.visibility = View.GONE
                        whats_empty.visibility = View.VISIBLE
                    }
                }
            }else{
                whatsEntity.clear()
                runBlocking { getSavedData() }
                if (whatsEntity.size > 0) {
                    whats_history.visibility = View.VISIBLE
                    whats_empty.visibility = View.GONE
                    whatsAdapter.setWhats(whatsEntity)
                } else {
                    whats_history.visibility = View.GONE
                    whats_empty.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun getSavedData() {
        val targetDirector = File(Environment.getExternalStorageDirectory().absolutePath + Constants().FOLDER_NAME)
        val files = targetDirector.listFiles()
//        Log.e("Statuses found", files.size.toString())
        try {
            for (i in files!!.indices) {
                val file = files[i]
                val type = if (Uri.fromFile(file).toString().endsWith(".mp4")) { "Video" } else { "Image" }

                val whats = WhatsEntity(0, file.name, files[i].absolutePath, Uri.fromFile(file).toString(),"", type, file.length().toString(), Legion().getCurrentDate())
                this.whatsEntity.add(whats)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}