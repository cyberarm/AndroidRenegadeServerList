package dev.cyberarm.renegade_server_list.game_server_hub.data

data class Status (
    val name: String,
    val password: Boolean,
    val map: String,
    val nextmap: String,
    val numplayers: Int,
    val maxplayers: Int,
    val started: String,
    val estimatedEndTime: String?,
    val remaining: String,
    val teams: Array<Team>,
    val players: Array<Player>
)