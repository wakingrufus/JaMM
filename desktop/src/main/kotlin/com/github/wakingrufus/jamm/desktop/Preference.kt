package com.github.wakingrufus.jamm.desktop

import java.util.prefs.Preferences

enum class Preference(val prefName: String) {
    LIBRARY_PATH("library.path"),
    CONTINUOUS_PLAY("continuous"),
    LASTFM_KEY("lastfm.session_key"),
    DARK_MODE("dark_mode")
}

fun getPreference(name: Preference, default: String): String {
    return Preferences.userNodeForPackage(Jamm::class.java).get(name.prefName, default)
}

fun putPreference(name: Preference, value: String) {
    Preferences.userNodeForPackage(Jamm::class.java).put(name.prefName, value)
}