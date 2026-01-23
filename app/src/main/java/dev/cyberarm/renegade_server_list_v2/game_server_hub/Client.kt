package dev.cyberarm.renegade_server_list_v2.game_server_hub

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.cyberarm.renegade_server_list_v2.Coordinator
import dev.cyberarm.renegade_server_list_v2.game_server_hub.data.List
import dev.cyberarm.renegade_server_list_v2.game_server_hub.data.Server
import okhttp3.OkHttpClient
import okhttp3.Request

class Client {
    val serverList: ArrayList<Server> = ArrayList()

    fun fetchServers(): Boolean
    {
        val client = OkHttpClient()
        val request = Request.Builder()
            .header("User-Agent", Coordinator.USER_AGENT)
            .url("https://gsh.w3d.cyberarm.dev/listings/getAll/v2?statusLevel=2")
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val body = response.body.string()
                println(body)
                println("Parsing...")
                val servers = Gson().fromJson(body, Array<Server>::class.java).toList()
                println("Parsed!")

                println(String.format("got servers: %d", servers.size))
                println(body)

                serverList.clear()
                serverList.addAll(servers)
                serverList.sortBy { it.status.numplayers }
                serverList.reverse()

                return true
            } else {
                return false
            }
        }
    }
}