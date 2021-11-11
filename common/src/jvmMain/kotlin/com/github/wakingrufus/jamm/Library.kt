package com.github.wakingrufus.jamm

class Library (
    val trackCount :Int = 0,
    val playlists : List<Playlist> = listOf(),
    val albumArtists : Map<AlbumArtist, Set<Album>> = mapOf(),
    val errors: List<String> = listOf(),
    val warnings: List<String> = listOf()
)