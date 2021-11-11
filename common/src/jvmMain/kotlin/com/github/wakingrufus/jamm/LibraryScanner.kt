package com.github.wakingrufus.jamm

import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
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
        val albumArtists: MutableMap<AlbumArtist, MutableSet<Album>> = mutableMapOf()
        val albumArtistsAlbums: MutableMap<AlbumArtist, MutableSet<String>> = mutableMapOf()
        val errors: MutableList<String> = mutableListOf()
        val warnings: MutableList<String> = mutableListOf()
        filesToProcess.forEach {
            if (it.name.endsWith(".m3u")) {
                playlistsMutableList.add(parse(rootDir, it))
            } else if (Extensions.music.contains(it.extension.toLowerCase())) {
                try {
                    AudioFileIO.logger.level = Level.WARNING
                    ID3v23Tag.logger.level = Level.WARNING
                    val f = AudioFileIO.read(it)
                    val tag: Tag = f.tag
                    tag.getFirst(FieldKey.ALBUM_ARTIST)
                    val albumArtist: AlbumArtist? = if (tag.hasField(FieldKey.ALBUM_ARTIST)) {
                        AlbumArtist(tag.getFirst(FieldKey.ALBUM_ARTIST))
                    } else if (tag.hasField(FieldKey.ARTIST)) {
                        warnings.add("${it.name} has no album-artist tag. falling back to artist")
                        AlbumArtist(tag.getFirst(FieldKey.ARTIST))
                    } else {
                        errors.add("${it.name} has no artist tag")
                        null
                    }
                    albumArtist?.run {
                        val albumName = tag.getFirst(FieldKey.ALBUM);
                        val newAlbum = albumArtistsAlbums.getOrPut(this){ mutableSetOf()}.add(albumName)
                        if(newAlbum) {
                            albumArtists.getOrPut(this) { mutableSetOf() }
                                .add(
                                    Album(
                                        this,
                                        tag.getFirst(FieldKey.ALBUM),
                                        tag.getFirst(FieldKey.ALBUM_YEAR)?.toIntOrNull()
                                            ?.let { LocalDate.of(it, 1, 1) },
                                        it.parentFile.resolve("cover.jpg").let {
                                            if (it.exists()) it.readBytes() else tag.firstArtwork.binaryData
                                        }
                                    ))
                        }
                    }
                } catch (e: Exception) {
                    errors.add("error processing file ${it.path} ${e.message} ${e.cause?.message}")
                }
            }
        }
        return Library(
            playlists = playlists,
            albumArtists = albumArtists,
            errors = errors,
            trackCount = filesToProcess.size
        )
    } else {
        return Library()
    }
}