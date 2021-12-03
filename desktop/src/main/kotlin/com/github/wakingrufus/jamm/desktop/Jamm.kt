package com.github.wakingrufus.jamm.desktop

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
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.File
import java.nio.file.Paths


class Jamm : Application(), Logging {

    val libraryPath: SimpleStringProperty = SimpleStringProperty(
        getPreference(Preference.LIBRARY_PATH, File(System.getProperty("user.home")).resolve("Music").path)
    )

    override fun start(primaryStage: Stage) {
        logger().info("starting")
        val f = Paths.get(libraryPath.value).toFile()
        if (!f.exists()) {
            logger().error("Music directory not found")
        } else {
            val observableLibrary = ObservableLibrary(f)

            val playQueue = FXCollections.observableArrayList<Track>()
            val mediaPlayerController = JfxMediaPlayerController(playQueue, observableLibrary)

            primaryStage.scene = scene<BorderPane>(width = 1920.0, height = 1080.0) {
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
                                        observableLibrary.scan()
                                        putPreference(Preference.LIBRARY_PATH, it)
                                    }
                                }
                            }
                            checkItem("Continuous Play") {
                                this.onAction = EventHandler {
                                    putPreference(Preference.CONTINUOUS_PLAY, this.isSelected.toString())
                                }
                                this.selectedProperty()
                                    .set(getPreference(Preference.CONTINUOUS_PLAY, "false").toBoolean())
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
                    tab("Now Playing") {
                        NowPlayingView(observableLibrary, mediaPlayerController, playQueue)
                    }
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
                bottom = PlayerBarView(observableLibrary, mediaPlayerController)
            }
//        val style = if (getPreference(Preference.DARK_MODE, "true").toBoolean()) "/dark.css" else "/light.css"
// primaryStage.scene.stylesheets.add(Jamm::class.java.getResource(style)?.path)
            primaryStage.show()
            observableLibrary.scan()
        }
    }
}