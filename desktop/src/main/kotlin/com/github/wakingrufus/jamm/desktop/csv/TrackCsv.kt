package com.github.wakingrufus.jamm.desktop.csv

import com.github.wakingrufus.jamm.common.AlbumArtist
import com.github.wakingrufus.jamm.common.AlbumKey
import com.github.wakingrufus.jamm.common.Artist
import com.github.wakingrufus.jamm.common.Track
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.time.LocalDate

fun CSVRecord.toTrack(baseDir: File): Track {
    val albumIdString = get("albumId")
    val tagsString = get("tags")
    return Track(
        title = get("title"),
        album = get("album"),
        albumArtist = AlbumArtist(get("albumArtist")),
        artist = Artist(get("artist")),
        albumKey = AlbumKey(albumIdString.ifBlank { null }, get("albumArtist"), get("album")),
        trackNumber = get("trackNumber").toIntOrNull(),
        discNumber = get("discNumber").toIntOrNull(),
        path = get("path"),
        tags = mutableSetOf(*(if (tagsString.isBlank()) emptyList() else tagsString.split("|")).toTypedArray()),
        releaseDate = parseDate(get("releaseDate")),
        musicBrainzTrackId = get("musicBrainzTrackId"),
        file = baseDir.resolve(get("path"))
    )
}

fun parseDate(s: String): LocalDate? {
    if (s == "null") {
        return null
    }
    return try {
        LocalDate.parse(s)
    } catch (e: Exception) {
        null
    }
}

class CsvField(val header: String, val getter: (Track) -> String)

val trackCsvFields = listOf(
    CsvField("title", Track::title),
    CsvField("album", Track::album),
    CsvField("albumArtist") { it.albumArtist.name },
    CsvField("trackNumber") { it.trackNumber.toString() },
    CsvField("discNumber") { it.discNumber.toString() },
    CsvField("artist") { it.artist.name },
    CsvField("albumId") { it.albumKey.id ?: "" },
    CsvField("releaseDate") { it.releaseDate.toString() },
    CsvField("path", Track::path),
    CsvField("tags") { it.tags.joinToString("|") },
    CsvField("musicBrainzTrackId") { it.musicBrainzTrackId ?: "" }
)