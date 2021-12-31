package com.github.wakingrufus.jamm.lastfm

import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.jamm.desktop.Logging
import com.github.wakingrufus.jamm.desktop.logger
import org.slf4j.Logger
import java.nio.charset.StandardCharsets
import java.time.Instant

class LastFmClient(val sessionKey: String) : Logging {
    fun scrobble(time: Instant, track: Track) {
        val args = mutableListOf(
            "artist" to track.artist.name,
            "track" to track.title,
            "album" to track.album,
            "albumArtist" to track.albumArtist.name,
            "timestamp" to time.epochSecond
        )
        track.trackNumber?.also {
            args.add("trackNumber" to it)
        }
        track.musicBrainzTrackId?.takeIf { it.isNotBlank() }?.also {
            args.add("mbid" to it)
        }
        signedPost("track.scrobble", sessionKey, args).response().also {
            it.logError(logger())
        }
    }

    fun nowPlaying(track: Track) {
        val args = mutableListOf<Pair<String, Any>>(
            "artist" to track.artist.name,
            "track" to track.title,
            "album" to track.album,
            "albumArtist" to track.albumArtist.name
        )
        track.trackNumber?.also {
            args.add("trackNumber" to it)
        }
        track.musicBrainzTrackId?.takeIf { it.isNotBlank() }?.also {
            args.add("mbid" to it)
        }
        signedPost("track.updatenowplaying", sessionKey, args).response().also {
            it.logError(logger())
        }
    }
}

fun ResponseResultOf<ByteArray>.logError(logger: Logger) {
    if (third.component2() != null) {
        logger.warn(third.component2()?.message)
        logger.warn(second.statusCode.toString())
        logger.warn(second.responseMessage)
        logger.warn(second.body().toByteArray().toString(StandardCharsets.UTF_8))
    }
}