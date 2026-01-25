package dev.cyberarm.renegade_server_list.settings.data

data class LegacyApplicationSettings(
    val renegadeUsername: String,
    val serviceAutoRefreshInterval: Int,
    val serviceAutoStartAtBoot: Boolean,
    val refreshOnMeteredConnections: Boolean,
    val globalServerSettings: LegacyServerSettings,
    val serverSettings: ArrayList<LegacyServerSettings>,
    val lastChangeLogVersion: Int
)
