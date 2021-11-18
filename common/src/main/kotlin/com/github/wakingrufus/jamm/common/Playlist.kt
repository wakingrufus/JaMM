package com.github.wakingrufus.jamm.common

import java.io.File

data class Playlist (val name: String, val playlistFile: File, val tracks: List<PlaylistTrack>)