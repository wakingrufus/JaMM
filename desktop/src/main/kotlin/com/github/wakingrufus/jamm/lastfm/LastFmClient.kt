package com.github.wakingrufus.jamm.lastfm

import com.github.kittinunf.fuel.core.ResponseResultOf
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.github.wakingrufus.jamm.common.Track
import mu.KotlinLogging
import java.nio.charset.StandardCharsets
import java.time.Instant

private val logger = KotlinLogging.logger {}

class LastFmClient(val sessionKey: String) {
    var username: String? = null
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
            it.logError()
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
            it.logError()
        }
    }

    fun getUserName(): String? {
        val result = signedPost("user.getInfo", sessionKey, emptyList()).responseJson()
        return when (result.third) {
            is Result.Success -> result.third.get().obj().getJSONObject("user").getString("name")
            is Result.Failure -> {
                logger.warn(result.second.responseMessage)
                logger.warn(result.second.body().toByteArray().toString(StandardCharsets.UTF_8))
                null
            }
        }
    }

    fun getPlaycount(track: Track): Int? {
        if (username == null) {
            username = getUserName()
            logger.debug { "user=$username" }
        }
        val args = mutableListOf<Pair<String, Any>>()
        username?.also {
            args.add("username" to it)
        }
        args.add("artist" to track.artist.name)
        args.add("track" to track.title)
//        track.musicBrainzTrackId?.takeIf { it.isNotBlank() }.also {
//            if (it != null) {
//                args.add("mbid" to it)
//            } else {
//                args.add("artist" to track.artist.name)
//                args.add("track" to track.title)
//            }
//        }
        val result = unAuthedSignedCall("track.getInfo", args).responseJson()
        return if (result.third is Result.Failure) {
            logger.warn(result.second.responseMessage)
            logger.warn(result.second.body().toByteArray().toString(StandardCharsets.UTF_8))
            null
        } else {
            val jsonObj = result.third.get().obj()
            if (jsonObj.has("error")) {
                logger.warn(result.third.get().obj().toString())
                null
            } else {
                result.third.get().obj().getJSONObject("track").getInt("userplaycount")
            }
        }
    }
}

fun ResponseResultOf<ByteArray>.logError() {
    if (third.component2() != null) {
        logger.warn(third.component2()?.message)
        logger.warn(second.statusCode.toString())
        logger.warn(second.responseMessage)
        logger.warn(second.body().toByteArray().toString(StandardCharsets.UTF_8))
    }
}