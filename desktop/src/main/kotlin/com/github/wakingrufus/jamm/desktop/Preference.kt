package com.github.wakingrufus.jamm.desktop

import java.util.*
import java.util.prefs.Preferences

enum class Preference(val prefName: String) {
    LIBRARY_PATH("library.path"),
    LASTFM_KEY("lastfm.session_key"),
    DARK_MODE("dark_mode"),
    CONTINUOUS_MODE("continuous.mode")
}

fun getPreference(name: Preference, default: String): String {
    return Preferences.userNodeForPackage(Jamm::class.java).get(name.prefName, default)
}

fun putPreference(name: Preference, value: String) {
    Preferences.userNodeForPackage(Jamm::class.java).put(name.prefName, value)
}

inline fun <reified T : Enum<T>> getPreference(name: Preference, default: T): T {
    val valueString = getPreference(name, default.toString())
    return EnumSet.allOf(T::class.java).firstOrNull { it.toString() == valueString } ?: default
}

enum class ContinuousMode {
    RANDOM, TAG, OFF
}