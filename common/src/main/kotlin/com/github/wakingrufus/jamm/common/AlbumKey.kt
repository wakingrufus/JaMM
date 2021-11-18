package com.github.wakingrufus.jamm.common

class AlbumKey(val id: String?, val albumArtist: String, val albumName: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlbumKey

        if (id != null && id == other.id) return true
        if (albumArtist != other.albumArtist) return false
        if (albumName != other.albumName) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: (31 * albumArtist.hashCode() + albumName.hashCode())
    }

    override fun toString(): String {
        return "AlbumKey(id=$id, albumArtist='$albumArtist', albumName='$albumName')"
    }
}