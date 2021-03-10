package com.example.android.architecture.blueprints.beetv.util

import com.example.android.architecture.blueprints.beetv.BeeTVApplication
import com.example.android.architecture.blueprints.beetv.R
import java.security.MessageDigest
import java.util.*
import java.util.concurrent.TimeUnit

fun String.toMD5(): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
    return bytes.toHex()
}
fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

fun String.formatID(): String = "${BeeTVApplication.context.applicationContext.getString(R.string.id)}: $this"


