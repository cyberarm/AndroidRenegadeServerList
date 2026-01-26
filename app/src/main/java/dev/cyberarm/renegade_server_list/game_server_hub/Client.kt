package dev.cyberarm.renegade_server_list.game_server_hub

import android.util.Log
import com.google.gson.Gson
import dev.cyberarm.renegade_server_list.Coordinator
import dev.cyberarm.renegade_server_list.game_server_hub.data.Server
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import java.util.concurrent.TimeUnit

class Client {
    private val CONNECTION_TIMEOUT_MS: Long = 5_000
    private val WRITE_TIMEOUT_MS: Long = 5_000
    private val READ_TIMEOUT_MS: Long = 5_000
    val serverList: ArrayList<Server> = ArrayList()

    fun fetchServers(): Boolean
    {
        try {
            val client = OkHttpClient().newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .build()

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

                    println(String.format(Locale.US, "got servers: %d", servers.size))
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
        } catch (e: Exception) {
            Log.e("Client", e.toString())

            return false
        }
    }
}