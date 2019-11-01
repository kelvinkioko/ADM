package com.download.manager.video.whatsapp.engine

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Legion {

    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat") val mdformat = SimpleDateFormat("dd-MM-yyyy")
        return mdformat.format(calendar.time)
    }

    fun getCurrentDateTime(): String {
        val calendar = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat") val mdformat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return mdformat.format(calendar.time)
    }

    fun getTimestamp(): String {
        val calendar = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat") val mdformat = SimpleDateFormat("yyyyMMdd_HHmmss")
        return mdformat.format(calendar.time)
    }

    fun getDownloadName(): String {
        val calendar = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat") val mdformat = SimpleDateFormat("ddHHmmss")
        return "Temp file" + mdformat.format(calendar.time)
    }

    fun getDate(): String {
        val calander = Calendar.getInstance()
        val cDay = calander.get(Calendar.DAY_OF_MONTH)
        val cMonth = calander.get(Calendar.MONTH) + 1
        val cYear = calander.get(Calendar.YEAR)

        return "$cYear-$cMonth-$cDay"
    }

    fun reFormatDate(day: String): String {
        val inFormat = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = null
        try {
            date = inFormat.parse(day)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        val outFormat = SimpleDateFormat("dd-MM-yyyy")
        return outFormat.format(date)
    }

    fun getTopTime(): String {
        val calendar = Calendar.getInstance()

        val day = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            else -> "Saturday"
        }

        val month = when (calendar.get(Calendar.MONTH) + 1){
            1 -> "JAN"
            2 -> "FEB"
            3 -> "MAR"
            4 -> "APR"
            5 -> "MAY"
            6 -> "JUN"
            7 -> "JUL"
            8 -> "AUG"
            9 -> "SEP"
            10 -> "OCT"
            11 -> "NOV"
            else -> "DEC"
        }

        val date = calendar.get(Calendar.DAY_OF_MONTH)

        return "$day, $month $date"
    }

    fun getAMPM(): String{
        val calendar = Calendar.getInstance()

        return if (calendar.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
    }

    fun randomNumber(min: Int, max: Int): Int {
        val range = Math.abs(max - min) + 1
        return (Math.random() * range).toInt() + if (min <= max) min else max
    }

    fun getConnectivity(context: Context): String {
        val manager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting
        val isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting
        var result = "null"
        if (is3g) {
            result = "mobile"
        } else if (isWifi) {
            result = "Wi-Fi"
        }

        return result
    }

    fun getModel(): String {
        return (Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name)
    }

    fun isStartDateBeforeEndDate(startDate: Date, endDate: Date): Boolean {
        return startDate.before(endDate)
    }

    fun isStartDateAfterEndDate(startDate: Date, endDate: Date): Boolean {
        return startDate.after(endDate)
    }

    fun getDays(start_Date: String, end_Date: String): Int {
        val formatter = DateTimeFormat.forPattern("dd-MM-yyyy")

        val startDate = formatter.parseDateTime(start_Date)
        val endDate = formatter.parseDateTime(end_Date)

        Log.e("Days", Days.daysBetween(startDate, endDate).days.toString())
        return Days.daysBetween(startDate, endDate).days
    }

    fun getWeeklyReviewRequestDays(lastRequestDate: String): Int {
        val calendar = Calendar.getInstance()
        @SuppressLint("SimpleDateFormat") val mdformat = SimpleDateFormat("dd-MM-yyyy")
        val formatter = DateTimeFormat.forPattern("dd-MM-yyyy")

        val strDate = formatter.parseDateTime(lastRequestDate)
        val strToday = formatter.parseDateTime(mdformat.format(calendar.time))
        return Days.daysBetween(strDate, strToday).days
    }

    fun startDateDisplay(year: Int, month: Int, day: Int): String {
        return (formatNumber(day, 2) + "-" + formatNumber(month + 1, 2)
                + "-" + year.toString())
    }

    private fun formatNumber(number: Int, digidCount: Int): String {
        var result = StringBuilder(Integer.toString(number))

        if (result.length > digidCount) {
            result = StringBuilder(result.substring(0, digidCount))
        }

        // fill with leading zero.
        var i = 0
        val size = digidCount - result.length
        while (i < size) {
            result.insert(0, "0")
            i++
        }
        return result.toString()
    }
}