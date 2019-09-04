package com.download.manager.video.whatsapp.ui.navigation

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.view.*
import android.widget.LinearLayout
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.ui.MainActivity
import kotlinx.android.synthetic.main.dialog_add_url.*
import kotlinx.android.synthetic.main.main_downloads.*
import java.util.*

class Downloads : Fragment() {

    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar!!.title = "Home | Downloads"

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
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_downloads, container, false)
    }

}