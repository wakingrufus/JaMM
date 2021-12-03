package com.github.wakingrufus.jamm.common

import java.io.File
import java.time.LocalDate

data class Track(
    val title: String,
    val album: String,
    val albumArtist: AlbumArtist,
    val trackNumber: Int?,
    val discNumber: Int?,
    val albumKey: AlbumKey,
    val file: File,
    val releaseDate: LocalDate? = null,
    val path: String,
    var image: ByteArray? = null,
    val tags: MutableSet<String>
)
