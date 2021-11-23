package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Album
import com.github.wakingrufus.jamm.common.AlbumArtist
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.beans.property.ReadOnlyListWrapper
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.image.Image
import javafx.scene.layout.*
import java.io.ByteArrayInputStream

class AlbumArtistView(val library: ObservableLibrary, val mediaPlayer: MediaPlayerController) : BorderPane(), Logging {
    val tracks = FXCollections.observableArrayList<Track>()
    val selectedAlbumArtist = SimpleObjectProperty<AlbumArtist>().also {
        it.onChange {
            albums.clear()
            albums.setAll(library.albumArtistsAlbums[it])
        }
    }
    val albums = FXCollections.observableArrayList<Album>()

    init {

        left<StackPane> {
            listview(library.albumArtistsAlbums.observableKeys().sorted(Comparator.comparing { it.name })) {
                this.cellFactory = CustomStringCellFactory { it.name }
                bindSelected(selectedAlbumArtist)
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
                            this.style = "-fx-font-family: 'DejaVu Sans', Arial, sans-serif;"
                        }
                        onMouseClicked = EventHandler {
                            tracks.clear()
                            tracks.setAll(library.albumTracks.get(album.albumKey)
                                ?.sortedBy { it.trackNumber }
                                ?.sortedBy { it.discNumber })
                        }
                    }
                }
            }
            bottom<BorderPane> {
                var tv: TableView<Track>? = null
                center<StackPane> {
                    tv = tableView(ReadOnlyListWrapper(tracks)) {
                        column<Track, Int>("Disc") { it.value.discNumber.toProperty() }
                        column<Track, Int>("Track #") { it.value.trackNumber.toProperty() }
                        column<Track, String>("Title") { it.value.title.toProperty() }
                        column<Track, String>("Album") { it.value.album.toProperty() }
                        column<Track, String>("Album Artist") { it.value.albumArtist.name.toProperty() }
                        autoResize()
                        this.selectionModel.selectionModeProperty().set(SelectionMode.MULTIPLE)
                    }
                }
                bottom<HBox> {
                    button("Play Album") {
                        action {
                            mediaPlayer.play(tracks)
                        }
                    }
                    button("Enqueue Album") {
                        action {
                            mediaPlayer.queue(tracks)
                        }
                    }
                    button("Play Selected") {
                        action {
                            tv?.selectionModel?.selectedItems?.run {
                                mediaPlayer.play(this)
                            }
                        }
                    }
                    button("Enqueue Selected") {
                        action {
                            tv?.selectionModel?.selectedItems?.run {
                                mediaPlayer.queue(this)
                            }
                        }
                    }
                }
            }
        }
    }
}