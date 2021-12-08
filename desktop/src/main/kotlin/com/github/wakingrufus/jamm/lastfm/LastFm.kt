package com.github.wakingrufus.jamm.lastfm

import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.github.wakingrufus.jamm.desktop.globalLogger
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object ApiKey {
    val applicationName = "JaMM"
    val key = "d7eea53c03e2110ca5cea5b682493a49"
    val sharedSecret = "4f5914ea6e4b52199eedbce950dff2ce"
}

class LastFmToken(val token: String)

fun getToken(): LastFmToken {
    val json = unAuthedSignedCall("auth.gettoken").responseJson()
    return LastFmToken(
        when (json.third) {
            is Result.Success -> json.third.get().obj().getString("token")
            is Result.Failure -> ""
        }
    )
}

fun getSession(token: LastFmToken): String {
    val json = unAuthedSignedCall("auth.getsession", listOf("token" to token.token)).responseJson()
    return when (json.third) {
        is Result.Success -> json.third.get().obj().getJSONObject("session").getString("key")
        is Result.Failure -> {
            globalLogger().warn(json.second.responseMessage)
            globalLogger().warn(json.second.body().toByteArray().toString(StandardCharsets.UTF_8))
            ""
        }
    }
}

fun requestAuthUrl(token: LastFmToken): String {
    return "https://www.last.fm/api/auth?api_key=${ApiKey.key}&token=${token.token}"
}

fun unAuthedSignedCall(method: String, params: List<Pair<String, Any>> = listOf()): Request {
    val allParams = params.plus(listOf("api_key" to ApiKey.key, "method" to method))
    val sig = requestSign(allParams)
    return "https://ws.audioscrobbler.com/2.0/"
        .httpGet(
            allParams
                .plus("format" to "json")
                .plus("api_sig" to sig)
        )
}

fun signedCall(method: String, sessionKey: String, params: List<Pair<String, Any>>): Request {
    val allParams =
        params.plus(listOf("api_key" to ApiKey.key, "method" to method, "sk" to sessionKey))
    val sig = requestSign(allParams)
    return "https://ws.audioscrobbler.com/2.0/"
        .httpGet(
            allParams
                .plus("format" to "json")
                .plus("api_sig" to sig)
        )
}

fun signedPost(method: String, sessionKey: String, params: List<Pair<String, Any>>): Request {
    val allParams =
        params.plus(listOf("api_key" to ApiKey.key, "method" to method, "sk" to sessionKey))
    val sig = requestSign(allParams)
    return "https://ws.audioscrobbler.com/2.0/"
        .httpPost(
            allParams
                .plus("format" to "json")
                .plus("api_sig" to sig)
        )
}

fun requestSign(params: List<Pair<String, Any>>): String {
    return params.sortedBy { it.first }.joinToString("") { it.first + it.second }
        .let { it + ApiKey.sharedSecret }
        .let {
            val md = MessageDigest.getInstance("MD5")
            val bytes = md.digest(it.toByteArray(charset = StandardCharsets.UTF_8))
            BigInteger(1, bytes).toString(16).padStart(32, '0')
//            val sb = StringBuilder()
//            for (b in bytes) {
//                sb.append(String.format("%02x", b))
//            }
//            sb.toString()
        }
}