package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Side
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File

class Jamm : Application(), Logging {

    val observableLibrary: ObservableLibrary = ObservableLibrary()

    override fun start(primaryStage: Stage) {
        logger().info("starting")
        var artistsTab: Tab? = null
        var playlistsTab: Tab? = null
        val playQueue = FXCollections.observableArrayList<Track>()
        //    var library = Library()
        val mediaPlayerController = object : MediaPlayerController {
            override fun play(tracks: List<Track>) {
                logger().info("adding ${tracks.size} to queue")
                playQueue.clear()
                playQueue.setAll(tracks)
            }

            override fun queue(tracks: List<Track>) {
                logger().info("adding ${tracks.size} to queue")
                playQueue.addAll(tracks)
            }
        }


        primaryStage.scene = scene<BorderPane>(width = 1920.0, height = 1080.0) {
            center<TabPane> {
                playlistsTab = Tab("Playlists")
                artistsTab = Tab("Album Artists")
                this.tabs.add(playlistsTab)
                this.tabs.add(artistsTab)
                this.side = Side.LEFT
            }
            val playNextQueue: () -> Unit = {

            }
            right<BorderPane> {
                top<BorderPane> {
                    this.center = MediaPlayerView(playQueue)
                }
                center<VBox> {
                    label("Play Queue")
                    add<VBox> {
                        this.children.bind(playQueue) { track ->
                            VBox().apply {
                                style = "-fx-border-color: white; -fx-border-style: solid; -fx-border-width: 1px;"
                                label(track.title) { style = "-fx-text-alignment: center;" }
                                label(track.albumArtist.name) {
                                    style = "-fx-font-weight: bold; -fx-text-alignment: center;"
                                }
                                label(track.album) { style = "-fx-font-style: italic; -fx-text-alignment: center;" }
                            }
                        }
                    }
                }
            }
            bottom<HBox> {

                buttonBar {
                    button("Load") {
                        action {
                            val libraryDir = File(System.getProperty("user.home"))
                                .resolve("Music")
                            if (!libraryDir.exists()) {
                                logger().error("Music directory not found")
                            } else {
                                //     GlobalScope.launch(Dispatchers.IO) {
//                                    library = scan(libraryDir)
//                                    library.warnings.forEach {
//                                        logger().warn(it)
//                                    }
//                                    library.errors.forEach {
//                                        logger().error(it)
//                                    }
                                observableLibrary.scan(libraryDir)
                                //        }
                                //     logger().info("tracks: ${observableLibrary.trackCount}")
                                logger().info("album artists: ${observableLibrary.albumArtistsAlbums.keys.size}")
                                artistsTab?.content = AlbumArtistView(observableLibrary, mediaPlayerController)
                                playlistsTab?.content = PlaylistView(observableLibrary)

                            }
                        }
                    }
                }
            }
        }
        primaryStage.show()
    }
}