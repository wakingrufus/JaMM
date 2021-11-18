package com.github.wakingrufus.jamm.common

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.ID3v23Tag
import java.io.File
import java.time.LocalDate
import java.util.logging.Level


object Extensions {
    val music = listOf("ogg", "mp3")
}

fun File.flatten(): List<File> = listFiles().flatMap {
    if (it.isDirectory) {
        it.flatten()
    } else {
        listOf(it)
    }
}

fun scan(rootDir: File): Library {
    if (rootDir.exists()) {
        val playlists = rootDir
            .listFiles { _: File?, s: String -> s.contains(".m3u") }
            ?.map { parse(rootDir, it) }
            ?: emptyList()
        val filesToProcess = rootDir.flatten()
        val playlistsMutableList: MutableList<Playlist> = mutableListOf()
        val albumArtistsAlbums: MutableMap<AlbumArtist, MutableSet<AlbumKey>> = mutableMapOf()
        val albums: MutableMap<AlbumKey, Album> = mutableMapOf()
        val albumTracks: MutableMap<AlbumKey, MutableList<Track>> = mutableMapOf()
        val errors: MutableList<String> = mutableListOf()
        val warnings: MutableList<String> = mutableListOf()
        val tracks: MutableList<Track> = mutableListOf()
        val trackPaths: MutableMap<String, Track> = mutableMapOf()
        filesToProcess.forEach { file ->
            if (file.name.endsWith(".m3u")) {
                playlistsMutableList.add(parse(rootDir, file))
            } else if (Extensions.music.contains(file.extension.toLowerCase())) {
                AudioFileIO.logger.level = Level.WARNING
                ID3v23Tag.logger.level = Level.WARNING
                val audioFile = AudioFileIO.read(file)
                val tr = buildTrack(rootDir, file, audioFile)
                when (tr) {
                    is ScanResult.ScanFailure -> errors.add(tr.error)
                    is ScanResult.TrackSuccess -> {
                        val track = tr.track
                        tracks.add(track)
                        trackPaths[file.toRelativeString(rootDir)] = track
                        val album = albums.computeIfAbsent(track.albumKey) {
                            buildAlbum(track).also {
                                albumArtistsAlbums.computeIfAbsent(it.artist) { mutableSetOf() }.add(track.albumKey)
                            }
                        }
                        if (album.coverImage == null) {
                            file.parentFile.resolve("cover.jpg").let {
                                album.coverImage = if (it.exists()) it.readBytes()
                                else track.image
                            }
                        }
                        if (track.image == null && album.coverImage != null) {
                            track.image = album.coverImage
                        }
                        albumTracks.computeIfAbsent(track.albumKey) {
                            mutableListOf()
                        }.add(track)
                    }
                }
            }
        }
        return Library(
            playlists = playlists,
            albumArtists = albumArtistsAlbums,
            errors = errors,
            warnings = warnings,
            trackCount = filesToProcess.size,
            albums = albums,
            tracks = tracks,
            trackPaths = trackPaths,
            albumTracks = albumTracks
        )
    } else {
        return Library()
    }
}

sealed class ScanResult {
    class TrackSuccess(val track: Track) : ScanResult()
    class PlaylistSuccess(val playlist: Playlist) : ScanResult()
    class ScanFailure(val error: String) : ScanResult()
    class AlbumCover() : ScanResult()
}

fun buildAlbum(track: Track): Album {
    return Album(
        albumKey = track.albumKey,
        artist = track.albumArtist,
        name = track.album,
        releaseDate = track.releaseDate
    )
}

fun buildTrack(rootFile: File, file: File, audioFile: AudioFile): ScanResult {
    if (audioFile.tag == null) {
        return ScanResult.ScanFailure("tag is null in file ${file.path}")
    }
    val tag = audioFile.tag
    val albumArtist = if (tag.hasField(FieldKey.ALBUM_ARTIST)) {
        AlbumArtist(tag.getFirst(FieldKey.ALBUM_ARTIST))
    } else if (tag.hasField(FieldKey.ARTIST)) {
        AlbumArtist(tag.getFirst(FieldKey.ARTIST))
    } else {
        AlbumArtist("*UNKNOWN*")
    }

    val albumName = if (tag.hasField(FieldKey.ALBUM)) tag.getFirst(FieldKey.ALBUM).let {
        it.ifBlank { "*UNKNOWN*" }
    } else "*UNKNOWN*"
    val releaseId =
        if (tag.hasField(FieldKey.MUSICBRAINZ_ORIGINAL_RELEASE_ID)) tag.getFirst(FieldKey.MUSICBRAINZ_ORIGINAL_RELEASE_ID)
            .let {
                it.ifBlank { null }
            }
        else null
    val albumKey = AlbumKey(
        id = releaseId,
        albumArtist = albumArtist.name,
        albumName = albumName
    )
    val track = Track(
        title = tag.getFirst(FieldKey.TITLE),
        album = albumName,
        albumArtist = albumArtist,
        trackNumber = tag.getFirst(FieldKey.TRACK).toIntOrNull(),
        albumKey = albumKey,
        file = file,
        releaseDate = tag.getFirst(FieldKey.ALBUM_YEAR)?.toIntOrNull()
            ?.let { LocalDate.of(it, 1, 1) },
        path= file.toRelativeString(rootFile),
        image = if (tag.artworkList.isNotEmpty()) {
            tag.firstArtwork?.binaryData
        } else null
    )
    return ScanResult.TrackSuccess(track)
}