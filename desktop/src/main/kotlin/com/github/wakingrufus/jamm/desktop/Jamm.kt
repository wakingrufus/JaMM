package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.AlbumKey
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Side
import javafx.scene.control.TabPane
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import java.io.File
import java.nio.file.Paths
import java.util.prefs.Preferences


class Jamm : Application(), Logging {

    val observableLibrary: ObservableLibrary = ObservableLibrary()
    val libraryPath: SimpleStringProperty = SimpleStringProperty(
        Preferences.userNodeForPackage(Jamm::class.java)
        .get("library.path", File(System.getProperty("user.home")).resolve("Music").path
    ))

    override fun start(primaryStage: Stage) {
        logger().info("starting")
        val playQueue = FXCollections.observableArrayList<Track>()
        //    var library = Library()
        val mediaPlayerView = MediaPlayerView(playQueue)
        val mediaPlayerController = object : MediaPlayerController {
            override fun play(tracks: List<Track>) {
                logger().info("adding ${tracks.size} to queue")
                playQueue.clear()
                playQueue.setAll(tracks)
                mediaPlayerView.play()
            }

            override fun queue(tracks: List<Track>) {
                logger().info("adding ${tracks.size} to queue")
                playQueue.addAll(tracks)
                mediaPlayerView.play()
            }
        }

        primaryStage.scene = scene<BorderPane>(width = 1920.0, height = 1080.0) {
            this.styleClass.add(JMetroStyleClass.BACKGROUND)
            top<VBox> {
                menuBar {
                    menu("Settings") {
                        item("Music Library...") {
                            this.onAction = EventHandler {
                                val pref = Preferences.userNodeForPackage(Jamm::class.java)
                                val current = pref.get(
                                    "library.path",
                                    File(System.getProperty("user.home")).resolve("Music").path
                                )
                                val dialog: TextInputDialog = TextInputDialog(current).apply {
                                    this.title = "Library Path"
                                }
                                dialog.contentText = "This is a sample dialog"
                                val path = dialog.showAndWait()
                                path.ifPresent {
                                    libraryPath.set(it)
                                    scan()
                                    pref.put("library.path", it)
                                }
                            }
                        }
                    }
                }
            }
            center<TabPane> {
                tab("Playlists") {
                    PlaylistView(observableLibrary)
                }
                tab("Album Artists") {
                    AlbumArtistView(observableLibrary, mediaPlayerController)
                }
                tab("Albums") {
                    AlbumsView(observableLibrary, mediaPlayerController)
                }
                this.side = Side.LEFT
            }
            right<BorderPane> {
                top<BorderPane> {
                    this.center = mediaPlayerView
                }
                center<VBox> {
                    label("Play Queue")
                    add<VBox> {
                        this.spacing = 10.0
                        this.children.bind(playQueue) { track ->
                            VBox().apply {
                                style = "-fx-border-color: white; -fx-border-style: solid; -fx-border-width: 1px;"
                                label(track.title) { style = "-fx-text-alignment: center;" }
                                this.children.add(HBox().apply {
                                    label(track.albumArtist.name) {
                                        style = "-fx-font-weight: bold; -fx-text-alignment: center;"
                                    }
                                    label(" - ")
                                    label(track.album) { style = "-fx-font-style: italic; -fx-text-alignment: center;" }
                                })

                            }
                        }
                    }
                }
            }
        }
        val jMetro = JMetro(Style.DARK)
        jMetro.scene = primaryStage.scene
        primaryStage.show()
        scan()
    }

    fun scan() {
        val f = Paths.get(libraryPath.value).toFile()
        if (!f.exists()) {
            logger().error("Music directory not found")
        } else {
            observableLibrary.scan(f)
        }
    }
}