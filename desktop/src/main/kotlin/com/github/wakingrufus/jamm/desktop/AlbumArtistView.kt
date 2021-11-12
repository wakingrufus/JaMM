package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.Album
import com.github.wakingrufus.jamm.AlbumArtist
import com.github.wakingrufus.jamm.Library
import com.github.wakingrufus.jamm.Track
import com.github.wakingrufus.javafx.bind
import com.github.wakingrufus.javafx.onChange
import com.github.wakingrufus.javafx.toProperty
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.image.Image
import javafx.scene.layout.*
import java.io.ByteArrayInputStream

class AlbumArtistView(val library: Library, val mediaPlayer: MediaPlayerController) : BorderPane() {
    val albumArtists = FXCollections.observableArrayList(library.albumArtists.keys.sortedBy { it.name })
    val tracks = FXCollections.observableArrayList<Track>()
    val selectedAlbumArtist = SimpleObjectProperty<AlbumArtist>().also {
        it.onChange {
            albums.clear()
            albums.setAll(library.albumArtists[it]?.map { library.albums[it] })
        }
    }
    val albums = FXCollections.observableArrayList<Album>()

    init {
        left<StackPane> {
            listview(albumArtists) {
                this.cellFactory = CustomStringCellFactory { it.name }
                bindSelected(selectedAlbumArtist)
            }
        }
        center<BorderPane> {
            center<TilePane> {
                this.children.bind(albums) { album ->
                    VBox().apply {
                        album.coverImage?.also {
                            imageView(Image(ByteArrayInputStream(it))) {
                                this.fitHeight = 256.0
                                this.fitWidth = 256.0
                            }
                        }
                        label(album.name)
                        onMouseClicked = EventHandler {
                            tracks.clear()
                            tracks.setAll(library.albumTracks.get(album.albumKey)?.sortedBy { it.trackNumber })
                        }
                    }
                }
            }
            bottom<VBox> {
                tableView(ReadOnlyListWrapper(tracks)) {
                    column<Track, String>("#") { it.value.trackNumber.toString().toProperty() }
                    column<Track, String>("Title") { it.value.title.toProperty() }
                    column<Track, String>("Album") { it.value.album.toProperty() }
                    column<Track, String>("Album Artist") { it.value.albumArtist.name.toProperty() }
                }
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