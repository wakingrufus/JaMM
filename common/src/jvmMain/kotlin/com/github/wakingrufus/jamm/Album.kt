package com.github.wakingrufus.jamm

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.tag.FieldKey
import java.time.LocalDate

class Album(
    val albumKey: AlbumKey,
    val artist: AlbumArtist,
    val name: String,
    val releaseDate: LocalDate?,
    var coverImage: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Album

        if (albumKey != other.albumKey) return false

        return true
    }

    override fun hashCode(): Int {
        return albumKey.hashCode()
    }

    override fun toString(): String {
        return "Album(albumKey=$albumKey, artist=$artist, name='$name', releaseDate=$releaseDate)"
    }
}
