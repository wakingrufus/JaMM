package com.github.wakingrufus.jamm

import java.io.File

data class Track(
    val title: String,
    val album: String,
    val albumArtist: AlbumArtist,
    val trackNumber: Int?,
    val albumKey: AlbumKey,
    val file: File,
    var image: ByteArray? = null
)
