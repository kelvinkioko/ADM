package com.download.manager.video.whatsapp.utility

import com.download.manager.video.whatsapp.utility.download.core.DownloadTask
import com.download.manager.video.whatsapp.utility.download.core.OnDownloadListener
import com.download.manager.video.whatsapp.utility.download.core.database.DownloaderDao
import com.download.manager.video.whatsapp.utility.download.helper.ConnectionHelper
import android.content.Context
import android.os.AsyncTask
import java.lang.ref.WeakReference
import java.net.MalformedURLException

/**
 * Author:  Alireza Tizfahm Fard
 * Date:    21/6/2019
 * Email:   alirezat775@gmail.com
 */

class Downloader private constructor(downloadTask: DownloadTask) : IDownload {

    //region field
    private var mDownloadTask: DownloadTask? = null
    //endregion

    //region initialize
    init {
        if (mDownloadTask == null)
            mDownloadTask = downloadTask
    }
    //endregion

    //region method interface
    override fun download() {
        if (mDownloadTask == null)
            throw IllegalAccessException("Rebuild new instance after \"pause or cancel\" download")
        mDownloadTask?.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun cancelDownload() {
        mDownloadTask?.cancel()
        mDownloadTask = null
    }

    override fun pauseDownload() {
        mDownloadTask?.pause()
        mDownloadTask = null
    }

    override fun resumeDownload() {
        mDownloadTask?.resume = true
        download()
    }
    //endregion

    class Builder(private val mContext: Context, private var mUrl: String, private var mType: String) {

        //region field
        private var mTimeOut: Int = 0
        private var mDownloadDir: String? = null
        private var mFileName: String? = null
        private var mExtension: String? = null
        private var mDownloadListener: OnDownloadListener? = null
        private var mHeader: Map<String, String>? = null
        //endregion

        /**
         * @param downloadDir for setting custom download directory (default value is sandbox/download/ directory)
         * @return builder
         */
        fun downloadDirectory(downloadDir: String): Builder {
            this.mDownloadDir = downloadDir
            return this
        }

        /**
         * @param downloadListener an event listener for tracking download events
         * @return builder
         */
        fun downloadListener(downloadListener: OnDownloadListener): Builder {
            this.mDownloadListener = downloadListener
            return this
        }

        /**
         * @param downloadListener remove listener
         * @return builder
         */
        
        fun removeDownloadListener(): Builder {
            this.mDownloadListener = null
            return this
        }

        /**
         * @param fileName  for saving with this name
         * @param extension extension of the file
         * @return builder
         */
        
        fun fileName(fileName: String, extension: String): Builder {
            this.mFileName = fileName
            this.mExtension = extension
            return this
        }

        /**
         * @param header for adding headers in http request
         * @return builder
         */
        
        fun header(header: Map<String, String>): Builder {
            this.mHeader = header
            return this
        }

        /**
         * @param timeOut is a parameter for setting connection time out.
         * @return Builder
         */
        
        fun timeOut(timeOut: Int): Builder {
            this.mTimeOut = timeOut
            return this
        }

        fun build(): Downloader {
            mUrl =
                if (mUrl.isEmpty()) throw MalformedURLException("The entered URL is not valid")
                else mUrl

            mDownloadDir =
                if (mDownloadDir == null || mDownloadDir!!.isEmpty()) mContext.getExternalFilesDir(null)?.toString()
                else mDownloadDir

            mTimeOut =
                if (mTimeOut == 0) ConnectionHelper.TIME_OUT_CONNECTION
                else mTimeOut

            val downloadTask = DownloadTask(
                mUrl,
                WeakReference(mContext),
                DownloaderDao.getInstance(mContext),
                mDownloadDir,
                mTimeOut,
                mDownloadListener,
                mHeader,
                mFileName,
                mType,
                mExtension
            )
            return Downloader(downloadTask)
        }
    }
}
