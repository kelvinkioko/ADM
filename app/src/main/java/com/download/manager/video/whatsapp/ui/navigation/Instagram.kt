package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import com.download.manager.video.whatsapp.ui.MainActivity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import com.download.manager.video.whatsapp.database.DatabaseApp
import com.download.manager.video.whatsapp.database.adapter.InstaAdapter
import com.download.manager.video.whatsapp.database.entity.InstaEntity
import com.download.manager.video.whatsapp.database.viewmodel.DownloadsViewModel
import com.download.manager.video.whatsapp.engine.Legion
import com.download.manager.video.whatsapp.widgets.StickyHeaderGridLayoutManager
import kotlinx.android.synthetic.main.dialog_add_url.*
import org.jsoup.Jsoup
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import android.support.v4.content.FileProvider
import com.download.manager.video.whatsapp.BuildConfig
import com.download.manager.video.whatsapp.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.dialog_how_to_instagram.*
import kotlinx.android.synthetic.main.main_gram.*
import java.io.File


class Instagram : Fragment(), InstaAdapter.OnItemClickListener  {

    lateinit var dialog: Dialog
    lateinit var saveDialog: Dialog
    private var parentUrl: String = ""
    private var postedBy: String = ""
    private var image: String = ""
    private var name: String = ""
    private var tempUrl: String = ""
    private var type: String = ""
    private var video: String = ""
    private var isError: Boolean = false
    private var isVideo: Boolean = false

    override fun parentClick(localUrl: String, type: String) {
        val videoFile = File(localUrl)
        val fileUri = FileProvider.getUriForFile(context!!, BuildConfig.APPLICATION_ID +".admprovider", videoFile)
        (activity as MainActivity).grantUriPermission("com.download.manager.video.whatsapp", fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val intent = Intent(Intent.ACTION_VIEW)
        if (type.equals("Image", true)){
            intent.setDataAndType(fileUri, "image/*")
        }else{
            intent.setDataAndType(fileUri, "video/*")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//DO NOT FORGET THIS EVER
        startActivity(intent)
    }

    private lateinit var downloadsViewModel: DownloadsViewModel
    private var instaEntity: MutableList<InstaEntity> = ArrayList()
    private lateinit var instaAdapter: InstaAdapter

    lateinit var _primary: LinearLayout
    lateinit var _success: LinearLayout
    lateinit var _error: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(activity is AppCompatActivity){
            (activity as AppCompatActivity).setSupportActionBar(toolbar)
        }
        (activity as MainActivity).supportActionBar!!.title = "Home | Insta"

        downloadsViewModel = ViewModelProviders.of(this).get(DownloadsViewModel::class.java)

        // Initialize the Mobile Ads SDK with an AdMob App ID.
        MobileAds.initialize(activity as MainActivity)

        // Create an ad request. If you're running this on a physical device, check your logcat to
        // learn how to enable test ads for it. Look for a line like this one:
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        ad_view.loadAd(adRequest)

        /**
         * Initializing adapter and layout manager for recyclerView
         */
        instaAdapter = InstaAdapter(activity as MainActivity, instaEntity)
        instaAdapter.setOnItemClickListener(this)
        val instaManager = StickyHeaderGridLayoutManager(2)
        instaManager.setHeaderBottomOverlapMargin(resources.getDimensionPixelSize(R.dimen.header_shadow_size))

        insta_history.layoutManager = instaManager
        insta_history.itemAnimator = DefaultItemAnimator()
        insta_history.adapter = instaAdapter

        main_add_insta.setOnClickListener{
            dialog = Dialog(activity)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_add_url)
            Objects.requireNonNull<Window>(dialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            dialog.window!!.setGravity(Gravity.BOTTOM)
            dialog.show()

            _primary = dialog.dau_primary
            _success = dialog.dau_success
            _error = dialog.dau_error

            _success.visibility = View.GONE
            _error.visibility = View.GONE

            val loader = dialog.dau_loader
            val title = dialog.dau_title
            val link_parent = dialog.dau_link_parent
            val link = dialog.dau_link
            val dismiss = dialog.dau_dismiss
            val done = dialog.dau_done

            val success_message = dialog.dau_success_message
            val success_dismiss = dialog.dau_success_dismiss

            val error_message = dialog.dau_error_message
            val error_dismiss = dialog.dau_error_dismiss
            val error_done = dialog.dau_error_done

            success_dismiss.setOnClickListener { dialog.dismiss() }
            error_dismiss.setOnClickListener { dialog.dismiss() }
            dismiss.setOnClickListener { dialog.dismiss() }
            error_done.setOnClickListener {
                _error.visibility = View.GONE
                _primary.visibility = View.VISIBLE
            }

            done.setOnClickListener {
                when {
                    link.text.toString().trim().startsWith("https://www.instagram.com/") -> {
                        title.visibility = View.GONE
                        link_parent.visibility = View.GONE
                        done.visibility = View.GONE
                        loader.visibility = View.VISIBLE

                        getInstaUrl().execute(link.text.toString().trim())
                    }
                    else -> Toast.makeText(activity, "Please enter a valid instagram url", Toast.LENGTH_LONG).show()
                }
            }
        }

        insta_help.setOnClickListener {
            saveDialog = Dialog(activity)
            saveDialog.setCanceledOnTouchOutside(false)
            saveDialog.setCancelable(true)
            saveDialog.setContentView(R.layout.dialog_how_to_instagram)
            Objects.requireNonNull<Window>(saveDialog.window).setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            saveDialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            saveDialog.window!!.setGravity(Gravity.BOTTOM)
            saveDialog.show()

            val dismiss = saveDialog.dc_dismiss

            dismiss.setOnClickListener { saveDialog.dismiss() }
        }
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
                            instaEntities[d].id, instaEntities[d].name, instaEntities[d].postedBy, instaEntities[d].parentUrl, instaEntities[d].liveUrl, instaEntities[d].localUrl, instaEntities[d].type, instaEntities[d].downloaded, instaEntities[d].size, instaEntities[d].datecreated
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

    inner class getInstaUrl : AsyncTask<String, String, String>() {

        /* access modifiers changed from: protected */
        public override fun onPreExecute() { super.onPreExecute() }

        /* access modifiers changed from: protected */
        public override fun doInBackground(vararg strings: String): String? {
            try {
                val doc = Jsoup.connect(strings[0]).get()
                image = doc.select("meta[property=og:image]").attr("content")
                video = doc.select("meta[property=og:video:secure_url]").attr("content")
                postedBy = doc.select("meta[property=og:description]").attr("content").split("@")[1].split("•")[0].trim()
                name = (Random().nextInt(899999999)).toString()
                isVideo = video.isNotEmpty()
            } catch (e: IOException) {
                isError = true
                isVideo = false
                e.printStackTrace()
            }
            return ""
        }

        /* access modifiers changed from: protected */
        public override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            val instant = if (isVideo) {
                InstaEntity(0, name, postedBy, parentUrl, video, "", "Video", "0", "0", Legion().getCurrentDate())
            } else {
                InstaEntity(0, name, postedBy, parentUrl, image, "", "Image", "1", "0", Legion().getCurrentDate())
            }
            if (DatabaseApp().getInstaDao(activity as MainActivity).countInstaListByUrl(instant.liveUrl) == 0) {
                DatabaseApp().getInstaDao(activity as MainActivity).insertInsta(instant)
            }
            dialog.dismiss()
        }
    }

}