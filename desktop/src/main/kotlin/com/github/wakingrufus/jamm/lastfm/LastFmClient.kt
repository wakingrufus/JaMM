package com.github.wakingrufus.jamm.lastfm

import com.github.wakingrufus.jamm.common.Track
import com.github.wakingrufus.jamm.desktop.Logging
import com.github.wakingrufus.jamm.desktop.logger
import java.nio.charset.StandardCharsets
import java.time.Instant

class LastFmClient(val sessionKey: String) : Logging {
    fun scrobble(time: Instant, track: Track) {
        val args = mutableListOf(
            "artist" to track.albumArtist.name,
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
            logger().info(it.second.responseMessage)
            logger().info(it.second.body().toByteArray().toString(StandardCharsets.UTF_8))
        }
    }

    fun nowPlaying(track: Track) {
        val args = mutableListOf<Pair<String, Any>>(
            "artist" to track.albumArtist.name,
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
            logger().info(it.second.responseMessage)
            logger().info(it.second.body().toByteArray().toString(StandardCharsets.UTF_8))
        }
    }
}
