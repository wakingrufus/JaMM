package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.*
import com.github.wakingrufus.javafx.*
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Side
import javafx.scene.control.ListView
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.io.File

class Main : Application(), Logging {
    override fun start(primaryStage: Stage) {
        logger().info("starting")
        val javaVersion: String = System.getProperty("java.version")
        val javafxVersion: String = System.getProperty("javafx.version")
        val playlists = FXCollections.observableArrayList<Playlist>()
        val albumArtists = FXCollections.observableArrayList<AlbumArtist>()
        val tracks = FXCollections.observableArrayList<Track>()
        val albums = FXCollections.observableArrayList<Album>()
        var artistsTab : Tab? = null

        primaryStage.scene = scene<BorderPane>(width = 1920.0, height = 1080.0) {
            center<TabPane> {

                    val playlistsTab = Tab("Playlists").apply {
                        this.content = ListView<Playlist>().apply {
                            this.bind(playlists) {
                                while (it.next()) {
                                    if (it.wasAdded()) {
                                        this.items.addAll(it.addedSubList)
                                    } else if (it.wasRemoved()) {
                                        this.items.removeAll(it.removed)
                                    }
                                }
                            }
                            this.cellFactory = CustomStringCellFactory { it.name }
                        }
                    }
                    artistsTab = Tab("Album Artists")
                    this.tabs.add(playlistsTab)
                    this.tabs.add(artistsTab)
                    this.side = Side.LEFT

            }
            right<VBox> {
                label("Play Queue")
                add<ListView<String>> {

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
                                    val library = scan(libraryDir)
                                    playlists.clear()
                                    playlists.setAll(library.playlists.sortedBy { it.name })
                                    albumArtists.clear()
                                    albumArtists.setAll(library.albumArtists.keys.sortedBy { it.name })
                                    library.warnings.forEach {
                                        logger().warn(it)
                                    }
                                    library.errors.forEach {
                                        logger().error(it)
                                    }
                                    logger().error("${library.trackCount}")
                                    artistsTab?.content = AlbumArtistView(library)
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

