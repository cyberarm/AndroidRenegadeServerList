package dev.cyberarm.renegade_server_list_v2.game_server_hub

import okhttp3.OkHttpClient
import okhttp3.Request

class Client {
    fun fetchServers()
    {
        val client = OkHttpClient()
        val request = Request.Builder()
            .header("User-Agent", "<>/<>")
            .url("https://gsh.w3d.cyberarm.dev/listings/getAll/v2?statusLevel=2")
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                println(response.body.string())
//                Gson().fromJson(response.body.string(), ArrayList<Server>())
            }
        }
    }
}