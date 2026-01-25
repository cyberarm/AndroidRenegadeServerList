package dev.cyberarm.renegade_server_list.settings.data

data class ServerSettings(
    val schema: Int,
    val uuid: String,
    val name: String,
    val notifyPlayerCount: Int,
    val notifyMapNames: ArrayList<String>,
    val notifyUsernames: ArrayList<String>,
    val notifyRequireMultipleConditions: Boolean
)
