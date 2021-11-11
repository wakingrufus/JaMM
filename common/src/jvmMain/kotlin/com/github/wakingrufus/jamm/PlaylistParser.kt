package com.github.wakingrufus.jamm

import java.io.File

fun parse(libDir: File, file: File): Playlist {
    return file.useLines(Charsets.UTF_8) {
        Playlist(name = file.nameWithoutExtension, tracks = it.map { libDir.resolve(it) }.toList(), playlistFile = file)
    }
}