package com.github.wakingrufus.jammdroid

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import java.io.File

fun getAllAudioFromDevice(context: Context): List<File> {
    val tempAudioList: MutableList<File> = ArrayList()
    val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Audio.AudioColumns._ID,
        MediaStore.Audio.AudioColumns.DATA
    )
    val c: Cursor? = context.contentResolver
        .query(uri, projection, MediaStore.Audio.Media.IS_MUSIC + "=1", arrayOf(), null)
    if (c != null) {
        while (c.moveToNext()) {
            val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, c.getLong(0))
            val file = contentUri.toFile()
            Log.i("contentUri :$contentUri", " file :${file.absolutePath}")
            tempAudioList.add(contentUri.toFile())
        }
        c.close()
    }
    return tempAudioList
}