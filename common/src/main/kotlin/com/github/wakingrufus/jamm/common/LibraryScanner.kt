package com.github.wakingrufus.jamm.common

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.tag.FieldKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

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

val scannerLogger: Logger = LoggerFactory.getLogger("scanner")

sealed class ScanResult {
    class TrackSuccess(val track: Track) : ScanResult()
    class ScanFailure(val error: String) : ScanResult()
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

    val artist = if(tag.hasField(FieldKey.ARTIST)){
        Artist(tag.getFirst(FieldKey.ARTIST))
    }else if (tag.hasField(FieldKey.ALBUM_ARTIST)) {
        Artist(tag.getFirst(FieldKey.ALBUM_ARTIST))
    }else {
        Artist("*UNKNOWN*")
    }

    val tags = tag.getFirst(FieldKey.TAGS).split(",").filter { it.isNotBlank() }

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
        albumArtist = albumArtist.name,
        albumName = albumName
    ).apply {
        id = releaseId
    }
    val originalYear = tag.getFirst(FieldKey.ORIGINAL_YEAR)
    val originalDate = tag.getFirst(FieldKey.ORIGINALRELEASEDATE)
    val year = tag.getFirst(FieldKey.YEAR)
    val parsedDates = listOf(originalDate, originalYear, year).map { it.parseDate() }
    parsedDates.filterIsInstance<DateParseFail>().forEach {
        scannerLogger.warn("invalid Date ${it.originalValue} in file ${file.name}")
    }
    val date = parsedDates.filterIsInstance<DateParseSuccess>().firstOrNull()?.date
    val track = Track(
        title = tag.getFirst(FieldKey.TITLE),
        album = albumName,
        artist = artist,
        albumArtist = albumArtist,
        trackNumber = tag.getFirst(FieldKey.TRACK).toIntOrNull(),
        albumKey = albumKey,
        discNumber = tag.getFirst(FieldKey.DISC_NO).toIntOrNull(),
        file = file,
        releaseDate = date,
        path = file.toRelativeString(rootFile),
        tags = tags.toMutableSet(),
        musicBrainzTrackId = tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)
    )
    return ScanResult.TrackSuccess(track)
}