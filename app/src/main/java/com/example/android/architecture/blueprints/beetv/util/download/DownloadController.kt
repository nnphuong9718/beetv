package com.example.android.architecture.blueprints.beetv.util.download

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Environment
import java.io.File
import java.util.*

class DownloadController {
    companion object {
        private var mInstance: DownloadController? = null
        fun getInstance(): DownloadController {
            if (mInstance == null) {
                mInstance = DownloadController()
            }
            return mInstance!!
        }
        val MIME_TYPE = "application/vnd.android.package-archive"
        val PROVIDER_PATH = ".provider"
        val GET = 0; // not have the app in device yet
        val OPEN = 1; // downloaded and installed the latest version of app in device
        val INSTALL = 2; // downloaded the app but not install in device yet.
        val UPDATE = 3; //
        val INSTALLED_SUFFIX: String = "_done";
        var IS_INSTALL = false;
        var IS_UNINSTALL = false;

        // 0 --> not downloaded
        // 1 --> installed and latest version
        // 2 --> download but have not install yet
        // 3 --> installed but old version
    }

    fun launchApp(context: Context, packageName: String?) {
        val intent = Intent()
        intent.setPackage(packageName)
        val resolveInfos = context.packageManager.queryIntentActivities(intent, 0)
        Collections.sort(resolveInfos, ResolveInfo.DisplayNameComparator(context.packageManager))
        if (resolveInfos.size > 0) {
            val launchable = resolveInfos[0]
            val activity = launchable.activityInfo
            val name = ComponentName(activity.applicationInfo.packageName,
                    activity.name)
            val i = Intent(Intent.ACTION_MAIN)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            i.component = name
            context.startActivity(i)
        }
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (exception: Exception) {
            false
        }
    }

    fun getApkPathByUrl(context: Context, apkUrl: String): String {
        val downloadUrl: String = apkUrl
        val strs = downloadUrl?.split("/")?.toTypedArray()
        val fileName: String = strs!![strs.size-1]
        var destination =
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += fileName
        return destination
    }

    fun getApkPathByName(context: Context, apkName: String): String {
        var destination =
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += apkName
        return destination
    }

    fun isApkDownloaded(context: Context, apkUrl: String): Boolean {
        val destination = getApkPathByUrl(context, apkUrl)
        val file = File(destination);
        if(file.exists()){
            return true
        }
        return false
    }

    fun getApkName(context: Context, apkUrl: String): String {
        val destination = getApkPathByUrl(context, apkUrl)
        val file = File(destination);
        if(file.exists()){
            return file.name
        }
        return ""
    }

    //
    fun getAppStatus(context: Context, packageName: String, apkUrl: String?): Int {
        if (isPackageInstalled(context, packageName)) {
            // check for latest version or old version:
            if (apkUrl != null) {
                val storageAppName: String? = getStorageAppName(context, apkUrl);
                if (storageAppName != null && storageAppName.contains("$INSTALLED_SUFFIX.apk")) {
                    return OPEN; // OPEN >< UNINSTALL
                } else {
                    return UPDATE; // UPDATE
                }
            }
        } else if (apkUrl != null && isApkDownloaded(context, apkUrl)) {
            return INSTALL; // INSTALL
        }
        return GET; // GET
    }

    fun getStorageAppName(context: Context, apkUrl: String) : String? {
        val downloadUrl: String = apkUrl
        val strs = downloadUrl?.split("/")?.toTypedArray()
        val fileName: String = strs!![strs.size-1]
        val fileNameArr = fileName.split(".apk");
        if (fileNameArr.isNotEmpty()) {
            val originalApkNameWithoutExtension = fileNameArr[0];
            val fullPathOriginApk = getApkPathByName(context, "$originalApkNameWithoutExtension.apk");
            val fullPathInstalledApk = getApkPathByName(context, "$originalApkNameWithoutExtension$INSTALLED_SUFFIX.apk");
            if (fullPathOriginApk != null) {
                val file = File(fullPathOriginApk);
                if(file.exists()){
                    return file.name
                }
            }
            if (fullPathInstalledApk != null) {
                val file = File(fullPathInstalledApk);
                if(file.exists()){
                    return file.name
                }
            }
        }
        return null;
    }

    fun isLatestVersion(context: Context, packageName: String, latestVersionCode: Long): Boolean {
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        return packageInfo.versionCode < latestVersionCode
    }

}