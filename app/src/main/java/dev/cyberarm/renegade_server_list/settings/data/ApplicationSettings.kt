package dev.cyberarm.renegade_server_list.settings.data

data class ApplicationSettings(
    val schema: Int,
    val inGameName: String,
    val serverListAutoRefreshEnabled: Boolean,
    val serverListRefreshIntervalInMinutes: Int,
    val serverListRefreshOnMeteredConnection: Boolean,
    val notifyServerSettings: ServerSettings,
    val serverSettings: ArrayList<ServerSettings>,
    val lastChangelogVersion: Int
)
