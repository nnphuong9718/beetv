package com.example.android.architecture.blueprints.beetv.util.download

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.io.*
import java.net.URL
import java.net.URLConnection

class DownloadTask(var context: Context, var progressBar: ProgressBar ?= null) : AsyncTask<String, String, String>() {

    var onSuccessListener : (() -> Unit)? = null
    var onFailedLister: (() -> Unit)? = null

    companion object {
        private const val BUFFER_SIZE: Int = 8192
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }

    override fun onCancelled(result: String?) {
        super.onCancelled(result)
        onFailedLister?.invoke()
    }

    override fun onProgressUpdate(vararg values: String?) {
        val percent = values[0]!!.toInt()
        progressBar?.progress = percent
        super.onProgressUpdate(*values)
    }

    override fun onPreExecute() {
//        onPreExecute(), invoked on the UI thread before the task is executed. This step is normally used to setup the task, for instance by showing a progress bar in the user interface.
        progressBar?.progress = 0
        progressBar?.secondaryProgress = 100
        progressBar?.isIndeterminate = false
        progressBar?.max = 100
        super.onPreExecute()
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        // ...
        progressBar?.progress = 100
        Log.d("DOWNLOAD_TASK", "download xong r ne")
        onSuccessListener?.invoke()
    }

    /**
     * Downloading file in background thread
     * */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun doInBackground(vararg params: String?): String? {
        try {
            var count : Int;
            val url = URL(params[0])
            val strs = params[0]?.split("/")?.toTypedArray()
            val fileName: String = strs!![strs.size-1]
            val connection: URLConnection = url.openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.connect()

            // this will be useful so that you can show a tipical 0-100%
            // progress bar
            val length: Long = connection.contentLengthLong
            // download the file
            val input: InputStream = BufferedInputStream(url.openStream(), BUFFER_SIZE)

            // output stream

            val destination = DownloadController.getInstance().getApkPathByName(context, fileName)
            val outputFile = File(destination);
            if(outputFile.exists()){
                Log.d("DOWNLOADTASK", "t dang existed r ne nha 1")

                outputFile.delete()
                if (outputFile.exists()) {
                    Log.d("DOWNLOADTASK", "t dang existed r ne nha 2")
                } else {
                    Log.d("DOWNLOADTASK", "t dang existed r ne nha 3")

                }
            }
            val output : OutputStream = FileOutputStream(destination)


            val data = ByteArray(1024)
            var total : Long = 0

            while (input.read(data).let { count = it; it != -1 }) {
                total += count
                // publishing the progress....
                // After this onProgressUpdate will be called
                val percent: Long = ((total * 100) / length)
                publishProgress("" + percent.toInt());

                // writing data to file
                output.write(data, 0, count)
            }

            // flushing output
            output.flush()

            // closing streams
            output.close()
            input.close()

//            val outputFile = File(destination);
//            if(outputFile.exists()){
//                outputFile.setReadable(true, false)
//            }
//            val storage: File = File(Environment.getExternalStorageDirectory() + File.separator + "/Image/"

        } catch (exception: Exception) {
            Log.e("Error: ", exception.message);
        }
        return null
    }
}
