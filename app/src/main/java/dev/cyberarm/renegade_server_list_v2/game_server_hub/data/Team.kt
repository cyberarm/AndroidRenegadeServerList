package dev.cyberarm.renegade_server_list_v2.game_server_hub.data

data class Team (
    val id: Int,
    val name: String,
    val score: Int,
    val kills: Int,
    val deaths: Int
)