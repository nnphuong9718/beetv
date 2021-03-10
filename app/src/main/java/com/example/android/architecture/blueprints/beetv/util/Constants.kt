package com.example.android.architecture.blueprints.beetv.util

class Constants {
    companion object {
        const val REGISTER = "REGISTER"
        const val LOGIN = "LOGIN"
        const val LIMIT  : Int = 15
    }

    enum class TYPE_CATEGORY(val type: String) {
        ALL("ALL"),
        TV("LIVE"),

        MOVIE("MOVIE"),
        DRAMA("DRAMA"),
        ENTERTAINMENT("ENTERTAINMENT"),
        EDUCATION("EDUCATION"),
        KID("KID"),

        FAVORITE("favorite"),
        PLAYBACK("playback"),
        SEARCH("search")
    }

    enum class TYPE_FILE(val type: String) {
        LIVE("LIVE"),
        MOVIE("MOVIE")
    }

    enum class TYPE_MENU(val type: Int) {
        MENU(1),
        CHANNEL(2),
        PROGRAM(3),
        CALENDAR(4)
    }

    enum class MediaResolution(val type: String) {
        AUTOMATION("automation"),
        R16P9("16:9"),
        R4P3("4:3"),
    }

}