package com.github.wakingrufus.jamm.common

class Library (
    val trackCount :Int = 0,
    val playlists : List<Playlist> = listOf(),
    val albumArtists : Map<AlbumArtist, Set<AlbumKey>> = mapOf(),
    val albums : Map<AlbumKey, Album> = mapOf(),
    val trackPaths : Map<String, Track> = mapOf(),
    val tracks: List<Track> = listOf(),
    val albumTracks  : Map<AlbumKey, List<Track>> = mapOf(),
    val errors: List<String> = listOf(),
    val warnings: List<String> = listOf()
)