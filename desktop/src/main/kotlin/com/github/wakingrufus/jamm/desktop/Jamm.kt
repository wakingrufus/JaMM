package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.javafx.*
import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.ScrollPane
import javafx.scene.control.TabPane
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.*
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.stage.Stage
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import java.io.File
import java.nio.file.Paths


class Jamm : Application(), Logging {

    val observableLibrary: ObservableLibrary = ObservableLibrary()
    val libraryPath: SimpleStringProperty = SimpleStringProperty(
        getPreference(Preference.LIBRARY_PATH, File(System.getProperty("user.home")).resolve("Music").path)
    )

    override fun start(primaryStage: Stage) {
        logger().info("starting")
        val playQueue = FXCollections.observableArrayList<Track>()
        //    var library = Library()
        val mediaPlayerView = MediaPlayerView(playQueue, observableLibrary)
        val mediaPlayerController = object : MediaPlayerController {
            override fun play(tracks: List<Track>) {
                logger().info("adding ${tracks.size} to queue")
                mediaPlayerView.stop()
                playQueue.clear()
                playQueue.addAll(tracks)
                mediaPlayerView.play()
            }

            override fun queue(tracks: List<Track>) {
                logger().info("adding ${tracks.size} to queue")
                playQueue.addAll(tracks)
            }
        }

        primaryStage.scene = scene<BorderPane>(width = 1920.0, height = 1080.0) {
            this.styleClass.add(JMetroStyleClass.BACKGROUND)
            top<VBox> {
                menuBar {
                    menu("Settings") {
                        item("Music Library...") {
                            this.onAction = EventHandler {
                                val current = getPreference(
                                    Preference.LIBRARY_PATH,
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
                                    putPreference(Preference.LIBRARY_PATH, it)
                                }
                            }
                        }
                        checkItem("Continuous Play") {
                            this.onAction = EventHandler {
                                putPreference(Preference.CONTINUOUS_PLAY, this.isSelected.toString())
                            }
                            this.selectedProperty().set(getPreference(Preference.CONTINUOUS_PLAY, "false").toBoolean())
                        }
                        checkItem("Dark Mode") {
                            this.onAction = EventHandler {
                                putPreference(Preference.DARK_MODE, this.isSelected.toString())
                            }
                            this.selectedProperty().set(getPreference(Preference.DARK_MODE, "true").toBoolean())
                        }
                    }
                }
            }
            center<TabPane> {
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                tab("Playlists") {
                    PlaylistView(observableLibrary)
                }
                tab("Album Artists") {
                    AlbumArtistView(observableLibrary, mediaPlayerController)
                }
                tab("Albums") {
                    AlbumsView(observableLibrary, mediaPlayerController)
                }
                tab("Tracks") {
                    TracksView(observableLibrary, mediaPlayerController)
                }
                tab("Tags") {
                    TagView(observableLibrary, mediaPlayerController)
                }
                this.side = Side.LEFT
            }
            right<BorderPane> {
                top<BorderPane> {
                    top<VBox> {
                        alignment = Pos.CENTER
                        label("Now Playing") {
                            font = Font.font(24.0)
                        }
                    }
                    this.center = mediaPlayerView
                }
                center<VBox> {
                    alignment = Pos.TOP_CENTER
                    label("Play Queue") {
                        alignment = Pos.CENTER
                        this.font = Font.font(24.0)
                    }
                    ScrollPane().apply {
                        this.content = VBox().apply {
                            this.spacing = 10.0
                            this.children.bind(playQueue) { track ->
                                VBox().apply {
                                    this.border = Border(
                                        BorderStroke(
                                            Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                                            BorderWidths(1.0)
                                        )
                                    )
                                    label(track.title)
                                    this.children.add(HBox().apply {
                                        label(track.albumArtist.name) {
                                            style = "-fx-font-weight: bold;"
                                        }
                                        label(" - ")
                                        label(track.album) {
                                            style = "-fx-font-style: italic;"
                                        }
                                    })

                                }
                            }
                        }
                    }.attachTo(this)
                }
            }
        }
        val style = if (getPreference(Preference.DARK_MODE, "true").toBoolean()) Style.DARK else Style.LIGHT
        val jMetro = JMetro(style)
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