package com.download.manager.video.whatsapp.engine

import android.content.Context
import android.content.SharedPreferences

class AdPreferrenceHandler(var context: Context) {

    private val privateMode = 0
    private val prefName = "AdHandler"

    private var pref: SharedPreferences = context.getSharedPreferences(prefName, privateMode)
    var editor: SharedPreferences.Editor = pref.edit()

    private val firstOpenCount = "firstOpenCount"
    private val viewSessionCount = "viewSessionCount"
    private val tabViewSessionCount = "tabViewSessionCount"
    private val downloadSessionCount = "downloadSessionCount"
    private val weeklyReviewSessionCount = "weeklyReviewSessionCount"

    fun setFirstOpenCount(isFirstOpenCount: Boolean) {
        editor.putBoolean(firstOpenCount, isFirstOpenCount)
        editor.commit()
    }
    fun getFirstOpenCount(): Boolean {
        return pref.getBoolean(firstOpenCount, true)
    }

    fun setViewSessionCount(isViewSessionCount: Int) {
        editor.putInt(viewSessionCount, isViewSessionCount)
        editor.commit()
    }
    fun getViewSessionCount(): Int {
        return pref.getInt(viewSessionCount, 0)
    }

    fun setTabViewSessionCount(isTabViewSessionCount: Int) {
        editor.putInt(tabViewSessionCount, isTabViewSessionCount)
        editor.commit()
    }
    fun getTabViewSessionCount(): Int {
        return pref.getInt(tabViewSessionCount, 0)
    }

    fun setDownloadSessionCount(isDownloadSessionCount: Int) {
        editor.putInt(downloadSessionCount, isDownloadSessionCount)
        editor.commit()
    }
    fun getDownloadSessionCount(): Int {
        return pref.getInt(downloadSessionCount, 0)
    }

    fun setWeeklyReviewSessionCount(isWeeklyReviewSessionCount: String) {
        editor.putString(weeklyReviewSessionCount, isWeeklyReviewSessionCount)
        editor.commit()
    }
    fun getWeeklyReviewSessionCount(): String? {
        return pref.getString(weeklyReviewSessionCount, "none")
    }
}