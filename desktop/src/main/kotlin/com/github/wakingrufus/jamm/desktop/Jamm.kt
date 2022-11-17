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
import javafx.geometry.Side
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import mu.KotlinLogging
import java.io.File
import java.nio.file.Paths


private val logger = KotlinLogging.logger {}

class Jamm : Application() {

    val libraryPath: SimpleStringProperty = SimpleStringProperty(
        getPreference(Preference.LIBRARY_PATH, File(System.getProperty("user.home")).resolve("Music").path)
    )
    val lastFmClientProperty = SimpleObjectProperty<LastFmClient>()
    override fun start(primaryStage: Stage) {
        logger.info("starting")
        val f = Paths.get(libraryPath.value).toFile()
        if (!f.exists()) {
            logger.error("Music directory not found")
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
                            actionItem("Rescan Music Library") {
                                observableLibrary.scan()
                            }
                            val lfm = actionItem("Last FM") {
                                getToken().also { token ->
                                    logger.info { "token: " + token.token }
                                    hostServices.showDocument(requestAuthUrl(token))
                                    val popup = Dialog<String>().apply {
                                        contentText = "Log in to last fm in your browser, then close this dialog"
                                        dialogPane.buttonTypes.add(ButtonType.CLOSE)
                                    }
                                    popup.showAndWait()
                                    getSession(token).run {
                                        logger.info("session key: $this")
                                        lastFmClientProperty.set(LastFmClient(this))
                                        putPreference(Preference.LASTFM_KEY, this)
                                    }
                                }
                            }
                            if (getPreference(Preference.LASTFM_KEY, "").isNotBlank()) {
                                lfm.isDisable = true
                                val sessionKey = getPreference(Preference.LASTFM_KEY, "")
                                logger.info("session key: $sessionKey")
                                lastFmClientProperty.set(LastFmClient(sessionKey))
                                actionItem("Reset Last.fm") {
                                    lastFmClientProperty.set(null)
                                    putPreference(Preference.LASTFM_KEY, "")
                                }
                            }
                            checkItem("Dark Mode") {
                                setOnAction {
                                    putPreference(Preference.DARK_MODE, this.isSelected.toString())
                                }
                                this.selectedProperty().set(getPreference(Preference.DARK_MODE, "true").toBoolean())
                            }
                            separator {

                            }
                            subMenu("Continuous Mode") {
                                toggleGroup {
                                    radio(ContinuousMode.OFF.toString()) {
                                        setOnAction {
                                            putPreference(Preference.CONTINUOUS_MODE, ContinuousMode.OFF.toString())
                                        }
                                        selectedProperty()
                                            .set(
                                                getPreference(
                                                    Preference.CONTINUOUS_MODE,
                                                    ContinuousMode.OFF
                                                ) == ContinuousMode.OFF
                                            )
                                    }
                                    radio(ContinuousMode.TAG.toString()) {
                                        setOnAction {
                                            putPreference(Preference.CONTINUOUS_MODE, ContinuousMode.TAG.toString())
                                        }
                                        selectedProperty()
                                            .set(
                                                getPreference(
                                                    Preference.CONTINUOUS_MODE,
                                                    ContinuousMode.OFF
                                                ) == ContinuousMode.TAG
                                            )
                                    }
                                    radio(ContinuousMode.RANDOM.toString()) {
                                        setOnAction {
                                            putPreference(Preference.CONTINUOUS_MODE, ContinuousMode.RANDOM.toString())
                                        }
                                        selectedProperty()
                                            .set(
                                                getPreference(
                                                    Preference.CONTINUOUS_MODE,
                                                    ContinuousMode.OFF
                                                ) == ContinuousMode.RANDOM
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
                val albumView = AlbumsView(observableLibrary, mediaPlayerController)
                val albumArtistView = AlbumArtistView(observableLibrary, mediaPlayerController)
                val tagView = TagView(observableLibrary, mediaPlayerController)
                var albumTab: Tab? = null
                var albumArtistTab: Tab? = null
                var tagTab: Tab? = null
                val tabPane = center<TabPane> {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("Now Playing") {
                        NowPlayingView(
                            library = observableLibrary,
                            mediaPlayerController = mediaPlayerController,
                            queue = playQueue,
                            viewAlbum = {
                                this.selectionModel.select(albumTab)
                                albumView.viewAlbum(it)
                            }, viewAlbumArtist = {
                                this.selectionModel.select(albumArtistTab)
                                albumArtistView.viewAlbumArtist(it)
                            }, viewTag = {
                                this.selectionModel.select(tagTab)
                                tagView.viewTag(it)
                            })
                    }
                    tab("Playlists") {
                        PlaylistView(observableLibrary, mediaPlayerController)
                    }
                    albumArtistTab = tab("Album Artists") {
                        albumArtistView
                    }
                    albumTab = tab("Albums") {
                        albumView
                    }
                    tab("Tracks") {
                        TracksView(observableLibrary, mediaPlayerController)
                    }
                    tagTab = tab("Tags") {
                        tagView
                    }
                    this.side = Side.LEFT
                }
                bottom = PlayerBarView(observableLibrary, mediaPlayerController, {
                    tabPane.selectionModel.select(albumTab)
                    albumView.viewAlbum(it)
                }, {
                    tabPane.selectionModel.select(albumArtistTab)
                    albumArtistView.viewAlbumArtist(it)
                })
            }
            val style = if (getPreference(Preference.DARK_MODE, "true").toBoolean()) "/dark.css" else "/light.css"
            primaryStage.scene.stylesheets.add(Jamm::class.java.getResource(style)?.toExternalForm())
            primaryStage.show()
            observableLibrary.importCsv()
            //      observableLibrary.scan()
        }
    }
}