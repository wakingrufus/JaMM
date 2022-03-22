package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.*
import com.github.wakingrufus.jamm.desktop.csv.toTrack
import com.github.wakingrufus.jamm.desktop.csv.trackCsvFields
import com.github.wakingrufus.jamm.library.LibraryListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.collections.ObservableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.id3.ID3v23Tag
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.logging.Level


class ObservableLibrary(val rootDir: File) : Logging {
    val playlists: ObservableList<Playlist> = FXCollections.observableArrayList()
    val trackPaths: ObservableMap<String, Track> = FXCollections.observableHashMap()
    val tracks: ObservableList<Track> = FXCollections.observableArrayList()
    val listeners: MutableList<LibraryListener> = mutableListOf()
    val tagListeners: MutableList<LibraryListener> = mutableListOf()

    fun clear() {
        GlobalScope.launch(Dispatchers.JavaFx) {
            playlists.clear()
            trackPaths.clear()
            tracks.clear()
        }
    }

    fun getAlbumArt(album: AlbumKey): ByteArray? {
        return tracks.firstOrNull { it.albumKey == album }?.let { getTrackArt(it) }
    }

    fun getTrackArt(track: Track): ByteArray? {
        val audioFile = AudioFileIO.read(track.file)
        return if (audioFile.tag.artworkList.isNotEmpty()) {
            audioFile.tag.firstArtwork?.binaryData
        } else if (track.file.parentFile.resolve("cover.jpg").exists()) {
            track.file.parentFile.resolve("cover.jpg").readBytes()
        } else if (track.file.parentFile.list { file, s -> s.endsWith(".jpg") }.isNotEmpty()) {
            track.file.parentFile.listFiles { file, s -> s.endsWith(".jpg") }.first().readBytes()
        } else {
            null
        }
    }

    fun exportTagPlaylist(tag: String) {
        val m3uFile = rootDir.resolve("tag-$tag.m3u")
        if (m3uFile.exists()) {
            m3uFile.delete()
        }
        m3uFile.createNewFile()
        m3uFile.writeText("#EXTM3U\n" +
                tracks.filter { it.tags.contains(tag) }.joinToString("\n") {
                    it.file.relativeTo(rootDir).path
                })
    }

    fun importCsv(){
        GlobalScope.launch(Dispatchers.Default) {
            val csvFile = rootDir.resolve("jamm.csv")
            val reader  = FileReader(csvFile)
            val headers = trackCsvFields.map { it.header }
            val records: Iterable<CSVRecord> = CSVFormat.DEFAULT
                .withHeader(*headers.toTypedArray())
                .withFirstRecordAsHeader()
                .parse(reader)
            val newTracks = records.map {
                it.toTrack(baseDir = rootDir)
            }
            logger().info("reading csv complete")
            withContext(Dispatchers.JavaFx) {
                newTracks.forEach {
                    importTrack(track = it)
                }
                logger().info("importing csv complete")
                listeners.forEach {
                    it.loadComplete()
                }
            }

        }
    }

    fun createCSVFile() {
        val csvFile = rootDir.resolve("jamm.csv")
        val out = FileWriter(csvFile)
        val headers = trackCsvFields.map { it.header }
        CSVPrinter(out, CSVFormat.DEFAULT.withHeader(*headers.toTypedArray())).use { printer ->
            tracks.forEach { track ->
                printer.printRecord(
                    trackCsvFields.map { it.getter.invoke(track) }
                )
            }
        }
    }

    fun setTags(track: Track, newTags: Set<String>) {
        GlobalScope.launch(Dispatchers.JavaFx) {
            track.tags.clear()
            track.tags.addAll(newTags)
            tagListeners.forEach {
                it.loadComplete()
            }
        }
        val audioFile = AudioFileIO.read(track.file)
        audioFile.tag.setField(FieldKey.TAGS, newTags.joinToString(","))
        AudioFileIO.write(audioFile)
    }

    fun importTrack(track: Track) {
        tracks.add(track)
        trackPaths[track.path] = track
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

    fun scan() {
        AudioFileIO.logger.level = Level.WARNING
        ID3v23Tag.logger.level = Level.WARNING

        val files = rootDir.flatten()
        logger().info("${files.size} files found")
        GlobalScope.launch(Dispatchers.Default) {
            val newPlayLists = files
                .filter { it.name.endsWith(".m3u") }
                .map { parse(rootDir, it) }
            withContext(Dispatchers.JavaFx) {
                newPlayLists.forEach { importPlaylist(it) }
            }
        }
        GlobalScope.launch(Dispatchers.Default) {
            val newTrackScans = files
                .filter { Extensions.music.contains(it.extension.toLowerCase()) }
                .chunked(1_000)
                .map {
                    it.map { readTrack(rootDir, it) }
                }
                .flatten()
            newTrackScans.filterIsInstance<ScanResult.ScanFailure>().forEach {  logger().error(it.error) }
            val newTracks = newTrackScans.filterIsInstance<ScanResult.TrackSuccess>().map {  it.track }
            withContext(Dispatchers.JavaFx) {
                trackPaths.clear()
                tracks.clear()
                newTracks.forEach { importTrack(it) }
            }
            listeners.forEach {
                it.loadComplete()
            }
            logger().info("scanned ${newTrackScans.size} tracks")
            createCSVFile()
        }
    }

    fun addListener(listener: LibraryListener){
        listeners.add(listener)
    }

    fun addTagListener(listener: LibraryListener){
        tagListeners.add(listener)
    }
}

