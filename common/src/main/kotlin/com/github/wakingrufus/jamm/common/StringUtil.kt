package com.github.wakingrufus.jamm.common

fun escapeNonAscii(str: String): String {
    val retStr = StringBuilder()
    var i = 0
    while (i < str.length) {
        val cp = Character.codePointAt(str, i)
        val charCount = Character.charCount(cp)
        if (charCount > 1) {
            i += charCount - 1 // 2.
            require(i < str.length) { "truncated unexpectedly" }
        }
        if (cp < 128) {
            retStr.appendCodePoint(cp)
        } else {
            retStr.append(String.format("\\u%x", cp))
        }
        i++
    }
    return retStr.toString()
}
