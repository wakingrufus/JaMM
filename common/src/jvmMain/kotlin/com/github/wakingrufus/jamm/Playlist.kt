package com.github.wakingrufus.jamm

import java.io.File

data class Playlist (val name: String, val playlistFile: File, val tracks: List<File>)