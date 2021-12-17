package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Album
import com.github.wakingrufus.jamm.common.AlbumArtist
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class AlbumArtistView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane(), Logging {
    val albumArtists: ObservableList<AlbumArtist> = FXCollections.observableArrayList()
    val tracks = FXCollections.observableArrayList<Track>()
    val selectedAlbumArtist = SimpleObjectProperty<AlbumArtist>().also {
        it.onChange { selectedAlbumArtist ->
            albums.clear()
            //    albums.setAll(library.albums.observableValues().filtered { it.artist == selectedAlbumArtist})
            albums.setAll(library.tracks.filter { it.albumArtist == selectedAlbumArtist }.groupBy { it.albumKey }
                .map { (key, tracks) ->
                    Album(
                        albumKey = key,
                        artist = AlbumArtist(key.albumArtist),
                        name = key.albumName,
                        tracks.mapNotNull { it.releaseDate }.firstOrNull(),
                        coverImage = library.getAlbumArt(key)
                    )
                }
                .toList().sortedBy { it.releaseDate })
        }
    }
    val albums = FXCollections.observableArrayList<Album>()
    lateinit var query: TextField
    fun applyFilter() {
        GlobalScope.launch(Dispatchers.Default) {
            val filtered = if (query.text.isNotBlank()) {
                library.tracks.grouped { it.albumArtist }
                    .filtered { it.name.toLowerCase().contains(query.text.toLowerCase()) }
            } else {
                library.tracks.grouped { it.albumArtist }
            }.sorted(Comparator.comparing { it.name.toLowerCase() })
            withContext(Dispatchers.JavaFx) {
                albumArtists.clear()
                albumArtists.setAll(filtered)
            }
        }
    }

    init {
        library.addListener { applyFilter() }
        left<BorderPane> {
            top<HBox> {
                query = add {
                    onAction = EventHandler {
                        applyFilter()
                    }
                }
            }
            center<StackPane> {
                //   listview(library.albumArtistsAlbums.observableKeys().sorted(Comparator.comparing { it.name })) {
                listview(albumArtists) {
                    this.cellFactory = CustomStringCellFactory { it.name }
                    bindSelected(selectedAlbumArtist)
                }
            }
        }
        center<BorderPane> {
            center<TilePane> {
                this.children.bind(albums) { album ->
                    VBox().apply {
                        maxWidth = 192.0
                        album.coverImage?.also {
                            imageView(Image(ByteArrayInputStream(it))) {
                                this.fitHeight = 192.0
                                this.fitWidth = 192.0
                            }
                        }
                        label(album.name) {
                            maxWidth = 192.0
                            this.isWrapText = true
                            this.style = "-fx-font-family: 'Noto Sans CJK JP';"
                        }
                        onMouseClicked = EventHandler {
                            tracks.clear()
                            tracks.setAll(library.tracks.filtered { it.albumKey == album.albumKey }
                                ?.sortedBy { it.trackNumber }
                                ?.sortedBy { it.discNumber })
                        }
                    }
                }
            }
            bottom<StackPane> {
                trackTable(tracks, library, mediaPlayer)
            }
        }
    }
}