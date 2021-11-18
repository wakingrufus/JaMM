package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Album
import com.github.wakingrufus.jamm.common.AlbumArtist
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.layout.*
import java.io.ByteArrayInputStream
import java.lang.reflect.InvocationTargetException


class AlbumArtistView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane(), Logging {
    val tracks = FXCollections.observableArrayList<Track>()
    val selectedAlbumArtist = SimpleObjectProperty<AlbumArtist>().also {
        it.onChange {
            albums.clear()
            albums.setAll(library.albumArtistsAlbums[it]?.map { library.albums[it] })
        }
    }
    val albums = FXCollections.observableArrayList<Album>()

    init {

        left<StackPane> {
            listview(library.albumArtists) {
                this.cellFactory = CustomStringCellFactory { it.name }
                bindSelected(selectedAlbumArtist)
            }
        }
        center<BorderPane> {
            center<TilePane> {
                this.children.bind(albums) { album ->
                    VBox().apply {
                        logger().info(album.name)
                        album.coverImage?.also {
                            imageView(Image(ByteArrayInputStream(it))) {
                                this.fitHeight = 256.0
                                this.fitWidth = 256.0
                            }
                        }
                        label(album.name) {
                            this.style = "-fx-font-family: 'DejaVu Sans', Arial, sans-serif;"
                        }
                        onMouseClicked = EventHandler {
                            tracks.clear()
                            tracks.setAll(library.albumTracks.get(album.albumKey)?.sortedBy { it.trackNumber })
                        }
                    }
                }
            }
            bottom<BorderPane> {
                center<StackPane> {
                    tableView(ReadOnlyListWrapper(tracks)) {
                        column<Track, String>("#") { it.value.trackNumber.toString().toProperty() }
                        column<Track, String>("Title") { it.value.title.toProperty() }
                        column<Track, String>("Album") { it.value.album.toProperty() }
                        column<Track, String>("Album Artist") { it.value.albumArtist.name.toProperty() }
                        autoResize()
                    }
                }
                bottom<HBox> {
                    button("Play") {
                        action {
                            mediaPlayer.play(tracks)
                        }
                    }
                }
            }
        }
    }
}