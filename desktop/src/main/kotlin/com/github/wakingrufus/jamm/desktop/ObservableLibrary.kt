package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.id3.ID3v23Tag
import java.io.File
import java.util.logging.Level
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path

class ObservableLibrary : Logging {
    val playlists: ObservableList<Playlist> = FXCollections.observableArrayList()
    val albums: ObservableMap<AlbumKey, Album> = FXCollections.observableHashMap()
    val trackPaths: ObservableMap<String, Track> = FXCollections.observableHashMap()
    val tracks: ObservableList<Track> = FXCollections.observableArrayList()
    val albumArtistsAlbums: ObservableMap<AlbumArtist, MutableSet<AlbumKey>> = FXCollections.observableHashMap()
    val albumTracks: ObservableMap<AlbumKey, ObservableList<Track>> = FXCollections.observableHashMap()
    val albumArtists: ObservableList<AlbumArtist> = FXCollections.observableArrayList()

    fun clear() {

    }

    fun importLibrary(library: Library) {

    }

    @OptIn(ExperimentalPathApi::class)
    fun importTrack(track: Track) {
        tracks.add(track)
        trackPaths[track.path] = track
        val album = albums.computeIfAbsent(track.albumKey) {
            buildAlbum(track).also {
                if (!albumArtists.contains(it.artist)) {
                    albumArtists.add(it.artist)
                    albumArtists.sortedBy { it.name }
                }
                albumArtistsAlbums.computeIfAbsent(it.artist) { mutableSetOf() }.add(track.albumKey)
            }
        }
        if (album.coverImage == null) {
            Path(track.path).toFile().parentFile.resolve("cover.jpg").let {
                album.coverImage = if (it.exists()) it.readBytes()
                else track.image
            }
        }
        if (track.image == null && album.coverImage != null) {
            track.image = album.coverImage
        }
        albumTracks.computeIfAbsent(track.albumKey) {
            FXCollections.observableArrayList()
        }.add(track)
    }

    fun importPlaylist(playlist: Playlist) {
        playlists.add(playlist)
    }

    suspend fun readTrack(rootDir: File, file: File): ScanResult = withContext(Dispatchers.IO) {
        this.runCatching {
            val audioFile = AudioFileIO.read(file)
            buildTrack(rootDir, file, audioFile)
        }.getOrElse { throwable -> ScanResult.ScanFailure(throwable.message ?: "error scanning file ${file.name}") }
    }

    fun scan(rootDir: File) {
        AudioFileIO.logger.level = Level.WARNING
        ID3v23Tag.logger.level = Level.WARNING
        clear()
        val files = rootDir.flatten()
        logger().info("${files.size} files found")
        files.asSequence().map {
            GlobalScope.launch(Dispatchers.Default) {
                val result = if (it.name.endsWith(".m3u")) {
                    ScanResult.PlaylistSuccess(parse(rootDir, it))
                } else if (Extensions.music.contains(it.extension.toLowerCase())) {
                    readTrack(rootDir, it)
                } else {
                    ScanResult.ScanFailure("unsupported file type: ${it.name}")
                }
                when (result) {
                    is ScanResult.ScanFailure -> logger().error(result.error)
                    is ScanResult.TrackSuccess -> {
                        importTrack(result.track)
                    }
                    is ScanResult.PlaylistSuccess -> {
                        importPlaylist(result.playlist)
                    }
                }
            }
        }.count()

    }
}