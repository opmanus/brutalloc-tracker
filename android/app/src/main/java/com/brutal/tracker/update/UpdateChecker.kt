package com.brutal.tracker.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class GitHubRelease(
    @SerializedName("tag_name") val tagName: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("body") val body: String = "",
    @SerializedName("assets") val assets: List<GitHubAsset> = emptyList()
)

data class GitHubAsset(
    @SerializedName("name") val name: String = "",
    @SerializedName("browser_download_url") val downloadUrl: String = "",
    @SerializedName("size") val size: Long = 0L
)

data class UpdateInfo(
    val version: String,
    val title: String,
    val downloadUrl: String,
    val notes: String
)

object UpdateChecker {
    private const val TAG = "UpdateChecker"
    private const val RELEASES_API = "https://api.github.com/repos/opmanus/brutalloc-tracker/releases/latest"
    private val gson = Gson()

    suspend fun checkForUpdates(currentVersion: String = "1.1.0"): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(RELEASES_API)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "ArkaTrack-Android")
                connectTimeout = 6000
                readTimeout = 6000
            }

            if (conn.responseCode == 200) {
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                val release = gson.fromJson(json, GitHubRelease::class.java)

                val latestTag = release.tagName.trim().removePrefix("v").trim()
                val currentClean = currentVersion.trim().removePrefix("v").trim()

                // Find release apk asset
                val apkAsset = release.assets.find { it.name.endsWith(".apk") && !it.name.contains("debug") }
                    ?: release.assets.firstOrNull { it.name.endsWith(".apk") }

                if (apkAsset != null && isNewerVersion(latestTag, currentClean)) {
                    Log.d(TAG, "Update found: $latestTag (current: $currentClean)")
                    return@withContext UpdateInfo(
                        version = release.tagName,
                        title = release.name.ifBlank { "Version ${release.tagName}" },
                        downloadUrl = apkAsset.downloadUrl,
                        notes = release.body
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking updates: ${e.message}")
        }
        return@withContext null
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        if (latest.isBlank() || current.isBlank()) return false
        try {
            val lParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
            val cParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(lParts.size, cParts.size)

            for (i in 0 until maxLen) {
                val l = lParts.getOrElse(i) { 0 }
                val c = cParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (e: Exception) {
            return latest != current
        }
        return false
    }

    fun startDownloadAndInstall(context: Context, downloadUrl: String, version: String) {
        try {
            val fileName = "ArkaTrack-$version.apk"
            val destination = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (destination.exists()) {
                destination.delete()
            }

            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Downloading ArkaTrack $version")
                setDescription("Updating application...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationUri(Uri.fromFile(destination))
                setMimeType("application/vnd.android.package-archive")
            }

            val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = manager.enqueue(request)

            val onCompleteReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id == downloadId) {
                        try {
                            ctxt.unregisterReceiver(this)
                            installApk(ctxt, destination)
                        } catch (e: Exception) {
                            Log.e(TAG, "Install launch error", e)
                        }
                    }
                }
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    onCompleteReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    onCompleteReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download manager error, fallback to browser", e)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        }
    }

    private fun installApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }
}
