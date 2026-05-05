package com.example.inventario.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

object UpdateManager {

    fun downloadAndInstallApk(context: Context, url: String, versionName: String) {
        try {
            val destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val fileName = "inventario_v$versionName.apk"
            val file = File(destination, fileName)
            
            // Si el archivo ya existe, lo borramos para bajar el nuevo
            if (file.exists()) file.delete()

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Actualizando Inventario")
                .setDescription("Descargando versión $versionName")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        installApk(context, file)
                        context.unregisterReceiver(this)
                    }
                }
            }

            ContextCompat.registerReceiver(
                context, 
                onComplete, 
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), 
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error al descargar: ${e.message}")
        }
    }

    private fun installApk(context: Context, file: File) {
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setDataAndType(contentUri, "application/vnd.android.package-archive")
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error al instalar: ${e.message}")
        }
    }
}
