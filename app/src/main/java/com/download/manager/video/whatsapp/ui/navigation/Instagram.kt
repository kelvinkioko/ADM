package com.download.manager.video.whatsapp.ui.navigation

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatDelegate
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.download.manager.video.whatsapp.R
import com.download.manager.video.whatsapp.ui.MainActivity
import android.content.Intent
import com.download.manager.video.whatsapp.utility.service.InstaService


class Instagram : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as MainActivity).supportActionBar!!.title = "Home | Insta"

        (activity as MainActivity).startService(Intent(activity, InstaService::class.java).setAction(InstaService().ACTION_START))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_gram, container, false)

    }

}