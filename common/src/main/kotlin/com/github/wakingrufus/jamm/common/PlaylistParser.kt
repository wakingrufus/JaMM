package com.github.wakingrufus.jamm.common

import java.io.File
import java.nio.file.Paths

fun parse(libDir: File, file: File): Playlist {
    return file.useLines(Charsets.UTF_8) { fileLines ->
        Playlist(
            name = file.nameWithoutExtension,
            tracks = fileLines
                .filter { it != "#EXTM3U" }
                .filter { !it.startsWith("#EXTINF") }
                .map { file.parentFile.resolve(it).canonicalFile }
                .mapIndexed { index, file ->
                    PlaylistTrack(
                        playlistTrackNumber = index,
                        pathRelativeToPlaylist = file.toRelativeString(file.parentFile),
                        pathRelativeToLibrary = file.toRelativeString(libDir)
                    )
                }
                .toList(),
            playlistFile = file
        )
    }
}

fun resolveAbsPath(playlistFile: File, path: String): File {
    val rel = playlistFile.parentFile.resolve(path)
    val abs = Paths.get(path).toFile()
    return if (rel.exists()) {
        rel.absoluteFile
    } else {
        abs
    }
}