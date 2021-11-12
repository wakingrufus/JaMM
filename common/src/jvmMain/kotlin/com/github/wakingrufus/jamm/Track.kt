package com.github.wakingrufus.jamm

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

data class Track(
    val title: String,
    val album: String,
    val albumArtist: AlbumArtist,
    val trackNumber: Int?,
    val albumKey: AlbumKey,
    val file: File
)
