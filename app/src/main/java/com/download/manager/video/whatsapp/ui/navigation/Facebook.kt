package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.DefaultItemAnimator
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.adapter.FaceAdapter
import com.download.manager.video.whatsapp.ui.MainActivity
import com.download.manager.video.whatsapp.database.entity.FaceEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.engine.PermissionListener
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridLayoutManager
import kotlinx.android.synthetic.main.dialog_add_url.*
import kotlinx.android.synthetic.main.main_facebook.*
import org.jsoup.Jsoup
import java.util.*
import kotlin.collections.ArrayList
import android.content.Context.MODE_PRIVATE
import android.os.Environment
import java.io.*


class Facebook : Fragment(), FaceAdapter.OnItemClickListener  {

    override fun parentClick(view: View, position: Int, userCode: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var faceEntity: MutableList<FaceEntity> = ArrayList()
    private lateinit var faceAdapter: FaceAdapter
    lateinit var dialog: Dialog
    private var parentUrl: String = ""
    private var postedBy: String = ""
    private var image: String = ""
    private var name: String = ""
    private var tempUrl: String = ""
    private var type: String = ""
    private var video: String = ""
    private var isError: Boolean = false
    private var isVideo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar!!.title = "Home | FB"

        PermissionListener(activity as MainActivity).loadPermissions()
        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

//        (activity as MainActivity).startService(Intent(activity, InstaService::class.java).setAction(InstaService().ACTION_START))

        /**
         * Initializing adapter and layout manager for recyclerView
         */
        faceAdapter = FaceAdapter(activity as MainActivity, faceEntity)
        faceAdapter.setOnItemClickListener(this)
        val faceManager = StickyHeaderGridLayoutManager(2)
        faceManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))

        face_history.layoutManager = faceManager
        face_history.itemAnimator = DefaultItemAnimator()
        face_history.adapter = faceAdapter

        main_add_facebook.setOnClickListener{
            dialog = Dialog(activity)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_add_url)
            Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.show()

            val loader = dialog.dau_loader
            val title = dialog.dau_title
            val link_parent = dialog.dau_link_parent
            val link = dialog.dau_link
            val dismiss = dialog.dau_dismiss
            val done = dialog.dau_done

            dismiss.setOnClickListener {
                dialog.dismiss()
            }

            done.setOnClickListener {
                title.visibility = View.GONE
                link_parent.visibility = View.GONE
                done.visibility = View.GONE
                loader.visibility = View.VISIBLE

                when {
                    link.text.toString().trim().startsWith("https://m.facebook.com/") -> getFaceUrl().execute(link.text.toString().trim())
                    link.text.toString().trim().startsWith("https://facebook.com/") -> getFaceUrl().execute(link.text.toString().trim())
                    link.text.toString().trim().startsWith("https://www.facebook.com/") -> getFaceUrl().execute(link.text.toString().trim())
                    else -> {
                        title.visibility = View.VISIBLE
                        link_parent.visibility = View.VISIBLE
                        done.visibility = View.VISIBLE
                        loader.visibility = View.GONE

                        Toast.makeText(activity, "Please enter a valid facebook url", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_facebook, container, false)
    }

    override fun onResume() {
        super.onResume()
        populateDownloads()
    }

    private fun populateDownloads(){
        downloadsViewModel.getFace().observe(this, Observer<List<FaceEntity>>{ faceEntities ->
            if (faceEntities != null){
                if (faceEntities.isNotEmpty()){
                    face_history.visibility = View.VISIBLE
                    face_empty.visibility = View.GONE

                    faceEntity.clear()
                    for (d in 0 until faceEntities.size){
                        val face = FaceEntity(
                            faceEntities[d].id, faceEntities[d].name, faceEntities[d].postedBy, faceEntities[d].imageUrl, faceEntities[d].videoUrl,
                            faceEntities[d].parentUrl, faceEntities[d].localUrl, faceEntities[d].type, faceEntities[d].downloaded, faceEntities[d].size, faceEntities[d].datecreated
                        )
                        this.faceEntity.add(face)
                    }
                    faceAdapter.setFace(faceEntity)
                }else{
                    face_history.visibility = View.GONE
                    face_empty.visibility = View.VISIBLE
                }
            }
        })
    }

    inner class getFaceUrl : AsyncTask<String, String, String>() {

        /** access modifiers changed from: protected */
        public override fun onPreExecute() { super.onPreExecute() }

        /** access modifiers changed from: protected */
        public override fun doInBackground(vararg strings: String): String? {
            try {
                val doc = Jsoup.connect(strings[0]).get()
                writeToFile(doc.toString())
                image = doc.select("meta[property=og:image]").attr("content")
                video = doc.select("meta[property=og:video]").attr("content")
                name = (Random().nextInt(899999999)).toString()
                isVideo = video.isNotEmpty()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return ""
        }

        /** access modifiers changed from: protected */
        public override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            if (isVideo) {
                tempUrl = video
                /** Save item in database */
                if (image.isNotEmpty()){
                    val face = FaceEntity(0, name, "", image, video, parentUrl, "", "Video", "0", "0", Legion().getCurrentDate())
                    DatabaseApp().getFaceDao(activity as MainActivity).insertFace(face)
                }
            } else {
                tempUrl = image
                /** Save item in database */
                if (image.isNotEmpty()){
                    val face = FaceEntity(0, name, "", image, video, parentUrl, "", "Image", "0", "0", Legion().getCurrentDate())
                    DatabaseApp().getFaceDao(activity as MainActivity).insertFace(face)
                }
            }
            populateDownloads()
            dialog.dismiss()
        }
    }

    private fun writeToFile(data: String) {
        val outputfile = File(Environment.getExternalStorageDirectory().toString() + File.separator + "Download Manager")
        if (!outputfile.exists()) { outputfile.mkdirs() }

        // Create the file.
        val file =  File(outputfile, "config.txt")

        try {
            file.createNewFile()

            val fOut = FileOutputStream(file)
            val myOutWriter = OutputStreamWriter(fOut)
            myOutWriter.append(data)

            myOutWriter.close()

            fOut.flush()
            fOut.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }

    }

}