package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.LinearLayout
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.adapter.DownloadsAdapter
import com.download.manager.video.whatsapp.database.entity.DownloadsEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.ui.MainActivity
import kotlinx.android.synthetic.main.dialog_add_url.*
import kotlinx.android.synthetic.main.main_downloads.*
import java.util.*
import kotlin.collections.ArrayList

class Downloads : Fragment(), DownloadsAdapter.OnItemClickListener {
    override fun parentClick(view: View, position: Int, userCode: String) {
        //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var downloadsEntity: MutableList<DownloadsEntity> = ArrayList()
    private lateinit var downloadsAdapter: DownloadsAdapter
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar!!.title = "Home | Downloads"

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        /**
         * Initializing adapter and layout manager for recyclerView
         */
        downloadsAdapter = DownloadsAdapter(activity as MainActivity, downloadsEntity)
        downloadsAdapter.setOnItemClickListener(this)
        val occupantManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        download_history.layoutManager = occupantManager
        download_history.itemAnimator = DefaultItemAnimator()
        download_history.adapter = downloadsAdapter

        populateList()

        main_add_download.setOnClickListener{
            dialog = Dialog(activity)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_add_url)
            Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.show()

            val link = dialog.dau_link
            val dismiss = dialog.dau_dismiss
            val done = dialog.dau_done

            dismiss.setOnClickListener {
                dialog.dismiss()
            }

            done.setOnClickListener {
                val downloadsEntity = DownloadsEntity(0, "Name of Download", link.text.toString().trim(), "0", "0", Legion().getCurrentDateTime())
                downloadsViewModel.insertDownloads(downloadsEntity)
                populateList()
                dialog.dismiss()
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_downloads, container, false)
    }

    private fun populateList(){
        downloadsViewModel.getDownloads().observe(this, Observer<List<DownloadsEntity>>{ downloadsEntities ->
            if (downloadsEntities != null){
                if (downloadsEntities.isNotEmpty()){
                    download_history.visibility = View.VISIBLE
                    download_empty.visibility = View.GONE

                    downloadsEntity.clear()
                    for (d in 0 until downloadsEntities.size){
                        val download = DownloadsEntity(
                            downloadsEntities[d].id, downloadsEntities[d].name, downloadsEntities[d].url, downloadsEntities[d].downloaded,
                            downloadsEntities[d].size, downloadsEntities[d].datecreated
                        )
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