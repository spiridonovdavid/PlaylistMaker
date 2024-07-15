package com.example.playlistmaker

import com.google.gson.Gson

fun getHistorySearch(appContext: App): MutableList<Track> {
    val arrayTrack = getDataSharedPrefs(appContext)
    return arrayTrack.asList().toMutableList()
}

fun clearHistory(appContext: App) {
    val json = Gson().toJson(emptyArray<Track>())
    appContext.sharedPrefs?.edit()
        ?.putString(HISTORY_SEARCH, json)
        ?.apply()
}

fun saveHistorySearch(appContext: App, newElement: Track) {
    val historyTrackList = getDataSharedPrefs(appContext).toMutableList()

    historyTrackList.removeIf { it.trackId == newElement.trackId }

    historyTrackList.add(0, newElement)

    val limitedListTrack = historyTrackList.take(10)

    val json = Gson().toJson(limitedListTrack.toTypedArray())
    appContext.sharedPrefs?.edit()?.putString(HISTORY_SEARCH, json)?.apply()
}

fun getDataSharedPrefs(appContext: App): Array<Track> {

    val json = appContext.sharedPrefs?.getString(HISTORY_SEARCH, null) ?: return emptyArray<Track>()
    var arrayTrack = Gson().fromJson(json, Array<Track>::class.java)

    arrayTrack.forEach {
        it.isHistory = true
    }

    return arrayTrack

}

