package com.download.manager.video.whatsapp.utility

class ArchivedCode {


//    inner class getFaceUrl : AsyncTask<String, String, String>() {
//
//        /* access modifiers changed from: protected */
//        public override fun onPreExecute() { super.onPreExecute() }
//
//        /* access modifiers changed from: protected */
//        public override fun doInBackground(vararg strings: String): String? {
//            try {
//                val doc = Jsoup.connect(strings[0]).get()
//                image = doc.select("meta[property=og:image]").attr("content")
//                video = doc.select("meta[property=og:video]").attr("content")
//                postedBy = doc.select("meta[property=og:description]").attr("content").split("@")[1].split("•")[0].trim()
//                name = (Random().nextInt(899999999)).toString()
//                isVideo = video.isNotEmpty()
//            } catch (e: IOException) {
//                isError = true
//                isVideo = false
//                e.printStackTrace()
//            }
//            return ""
//        }
//
//        /* access modifiers changed from: protected */
//        public override fun onPostExecute(s: String) {
//            super.onPostExecute(s)
//            if (isVideo) {
//                tempUrl = video
//                /*** Save item in database*/
//                if (image.isNotEmpty()){
//                    val face = FaceEntity(0, name, postedBy, image, video, parentUrl, "", "Video", "0", "0", Legion().getCurrentDate())
//                    DatabaseApp().getFaceDao(applicationContext).insertFace(face)
//                }
//            } else {
//                tempUrl = image
//                /** Save item in database */
//                if (image.isNotEmpty()){
//                    val face = FaceEntity(0, name, postedBy, image, video, parentUrl, "", "Image", "1", "0", Legion().getCurrentDate())
//                    DatabaseApp().getFaceDao(applicationContext as MainActivity).insertFace(face)
//                }
//            }
//        }
//    }

}