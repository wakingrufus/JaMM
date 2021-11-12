package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.Library
import com.github.wakingrufus.jamm.Track
import com.github.wakingrufus.jamm.scan
import com.github.wakingrufus.javafx.*
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Side
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File

class Main : Application(), Logging {
    override fun start(primaryStage: Stage) {
        logger().info("starting")
        var artistsTab: Tab? = null
        var playlistsTab: Tab? = null
        val playQueue = FXCollections.observableArrayList<Track>()
        var library = Library()

        primaryStage.scene = scene<BorderPane>(width = 1920.0, height = 1080.0) {
            center<TabPane> {
                playlistsTab = Tab("Playlists")
                artistsTab = Tab("Album Artists")
                this.tabs.add(playlistsTab)
                this.tabs.add(artistsTab)
                this.side = Side.LEFT
            }
            right<VBox> {
                label("Play Queue")
                this.children.bind(playQueue) { track ->
                    VBox().apply {
                        imageView(Image(ByteArrayInputStream(library.albums.get(track.albumKey)?.coverImage))) {
                            this.fitHeight = 32.0
                            this.fitWidth = 32.0
                        }
                        label(track.title)
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
                                GlobalScope.launch(Dispatchers.JavaFx) {
                                    library = scan(libraryDir)
                                    library.warnings.forEach {
                                        logger().warn(it)
                                    }
                                    library.errors.forEach {
                                        logger().error(it)
                                    }
                                    logger().info("tracks: ${library.trackCount}")
                                    logger().info("album artists: ${library.albumArtists.keys.size}")
                                    artistsTab?.content = AlbumArtistView(library)
                                    playlistsTab?.content = PlaylistView(library)
                                }
                            }
                        }
                    }

                    add<Text> {
                        this.text = "test 1"
                    }
                }
            }
        }
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java, *args)
        }
    }
}
