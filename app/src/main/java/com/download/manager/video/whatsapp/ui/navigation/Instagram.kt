package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.ui.MainActivity
import android.content.Intent
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import com.download.manager.video.whatsapp.database.adapter.InstaAdapter
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.utility.service.InstaService
import kotlinx.android.synthetic.main.main_gram.*
import android.support.v7.widget.StaggeredGridLayoutManager
import com.download.manager.video.whatsapp.database.adapter.GridAdapter
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridLayoutManager

class Instagram : Fragment(), GridAdapter.OnItemClickListener  {

    override fun parentClick(view: View, position: Int, userCode: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var instaEntity: MutableList<InstaEntity> = ArrayList()
    private lateinit var instaAdapter: GridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar!!.title = "Home | Insta"

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        (activity as MainActivity).startService(Intent(activity, InstaService::class.java).setAction(InstaService().ACTION_START))

        /**
         * Initializing adapter and layout manager for recyclerView
         */
        instaAdapter = GridAdapter(activity as MainActivity, instaEntity)
        instaAdapter.setOnItemClickListener(this)
        val instaManager = StickyHeaderGridLayoutManager(3)
        instaManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))

        insta_history.layoutManager = instaManager
        insta_history.itemAnimator = DefaultItemAnimator()
        insta_history.adapter = instaAdapter

        populateDownloads()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_gram, container, false)

    }

    override fun onResume() {
        super.onResume()
        populateDownloads()
    }

    private fun populateDownloads(){
        downloadsViewModel.getInsta().observe(this, Observer<List<InstaEntity>>{ instaEntities ->
            if (instaEntities != null){
                if (instaEntities.isNotEmpty()){
                    insta_history.visibility = View.VISIBLE
                    insta_empty.visibility = View.GONE

                    instaEntity.clear()
                    for (d in 0 until instaEntities.size){
                        val insta = InstaEntity(
                            instaEntities[d].id, instaEntities[d].name, instaEntities[d].postedBy, instaEntities[d].imageUrl, instaEntities[d].videoUrl,
                            instaEntities[d].parentUrl, instaEntities[d].localUrl, instaEntities[d].type, instaEntities[d].size, instaEntities[d].datecreated
                        )
                        this.instaEntity.add(insta)
                    }
                    instaAdapter.setInsta(instaEntity)
                }else{
                    insta_history.visibility = View.GONE
                    insta_empty.visibility = View.VISIBLE
                }
            }
        })
    }

}