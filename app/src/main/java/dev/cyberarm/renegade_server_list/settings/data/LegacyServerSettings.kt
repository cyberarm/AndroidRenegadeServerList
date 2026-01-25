package dev.cyberarm.renegade_server_list.settings.data

data class LegacyServerSettings(
    val ID: String,
    val name: String,
    val notifyPlayerCount: Int,
    val notifyMapNames: ArrayList<String>,
    val notifyUsernames: ArrayList<String>,
    val notifyRequireMultipleConditions: Boolean
)