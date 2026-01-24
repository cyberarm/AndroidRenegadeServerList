package dev.cyberarm.renegade_server_list.game_server_hub.data

data class Player (
    val nick: String,
    val team: Int,
    val score: Int,
    val kills: Int,
    val deaths: Int,
    val ping: Int,
    val time: String
)
