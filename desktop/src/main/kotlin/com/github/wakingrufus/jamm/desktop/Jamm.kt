package com.github.wakingrufus.jamm.desktop

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.jamm.lastfm.LastFmClient
import com.github.wakingrufus.jamm.lastfm.getSession
import com.github.wakingrufus.jamm.lastfm.getToken
import com.github.wakingrufus.jamm.lastfm.requestAuthUrl
import com.github.wakingrufus.javafx.*
import javafx.application.Application
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.Side
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.File
import java.nio.file.Paths


class Jamm : Application(), Logging {

    val libraryPath: SimpleStringProperty = SimpleStringProperty(
        getPreference(Preference.LIBRARY_PATH, File(System.getProperty("user.home")).resolve("Music").path)
    )
    val lastFmClientProperty = SimpleObjectProperty<LastFmClient>()
    override fun start(primaryStage: Stage) {
        logger().info("starting")
        val f = Paths.get(libraryPath.value).toFile()
        if (!f.exists()) {
            logger().error("Music directory not found")
        } else {
            val observableLibrary = ObservableLibrary(f)

            val playQueue = FXCollections.observableArrayList<Track>()
            val mediaPlayerController = JfxMediaPlayerController(playQueue, observableLibrary, lastFmClientProperty)

            primaryStage.scene = scene<BorderPane>(width = 1920.0, height = 1080.0) {
                top<VBox> {
                    menuBar {
                        menu("Settings") {
                            actionItem("Music Library...") {
                                DirectoryChooser().apply {
                                    this.initialDirectory = File(libraryPath.get())
                                    this.title = "Library Path"
                                }.run {
                                    val newPath = showDialog(primaryStage)
                                    newPath?.also {
                                        libraryPath.set(it.path)
                                        observableLibrary.scan()
                                        putPreference(Preference.LIBRARY_PATH, it.path)
                                    }
                                }
                            }
                            val lfm = actionItem("Last FM") {
                                getToken().also { token ->
                                    logger().info("token: " + token.token)
                                    hostServices.showDocument(requestAuthUrl(token))
                                    val popup = Dialog<String>().apply {
                                        contentText = "Log in to last fm in your browser, then close this dialog"
                                        dialogPane.buttonTypes.add(ButtonType.CLOSE)
                                    }
                                    popup.showAndWait()
                                    getSession(token).run {
                                        logger().info("session key: $this")
                                        lastFmClientProperty.set(LastFmClient(this))
                                        putPreference(Preference.LASTFM_KEY, this)
                                    }
                                }
                            }
                            if (getPreference(Preference.LASTFM_KEY, "").isNotBlank()) {
                                lfm.isDisable = true
                                val sessionKey = getPreference(Preference.LASTFM_KEY, "")
                                logger().info("session key: $sessionKey")
                                lastFmClientProperty.set(LastFmClient(sessionKey))
                                actionItem("Reset Last.fm") {
                                    lastFmClientProperty.set(null)
                                    putPreference(Preference.LASTFM_KEY, "")
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
                        PlaylistView(observableLibrary, mediaPlayerController)
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
            val style = if (getPreference(Preference.DARK_MODE, "true").toBoolean()) "/dark.css" else "/light.css"
            primaryStage.scene.stylesheets.add(Jamm::class.java.getResource(style)?.toExternalForm())
            primaryStage.show()
            observableLibrary.scan()
        }
    }
}