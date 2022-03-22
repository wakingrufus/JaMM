package com.github.wakingrufus.jammdroid

class AudioModel {
    var aPath: String? = null
    var aName: String? = null
    var aAlbum: String? = null
    var aArtist: String? = null
    fun getaPath(): String? {
        return aPath
    }

    fun setaPath(aPath: String?) {
        this.aPath = aPath
    }

    fun getaName(): String? {
        return aName
    }

    fun setaName(aName: String?) {
        this.aName = aName
    }

    fun getaAlbum(): String? {
        return aAlbum
    }

    fun setaAlbum(aAlbum: String?) {
        this.aAlbum = aAlbum
    }

    fun getaArtist(): String? {
        return aArtist
    }

    fun setaArtist(aArtist: String?) {
        this.aArtist = aArtist
    }
}