package dev.cyberarm.renegade_server_list_v2.game_server_hub.data

data class Server (
    val id: String,
    val game: String,
    val channel: String,
    val address: String,
    val port: Int,
    val region: String,
    val version: String,
    val status: Status
)