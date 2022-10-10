package com.github.wakingrufus.jamm.common

import java.io.File
import java.time.LocalDate

data class Track(
    val title: String,
    val album: String,
    val albumArtist: AlbumArtist,
    val artist: Artist,
    val trackNumber: Int?,
    val discNumber: Int?,
    val albumKey: AlbumKey,
    val file: File,
    val releaseDate: LocalDate? = null,
    val path: String,
    val tags: MutableSet<String>,
    val musicBrainzTrackId: String?,
    var playCount: Int = 0
)
