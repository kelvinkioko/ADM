package com.download.manager.video.whatsapp.utility

/**
 * Author:  Alireza Tizfahm Fard
 * Date:    21/6/2019
 * Email:   alirezat775@gmail.com
 */

internal interface IDownload {
    fun download()
    fun cancelDownload()
    fun pauseDownload()
    fun resumeDownload()
}