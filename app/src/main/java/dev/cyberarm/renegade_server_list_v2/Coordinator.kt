package dev.cyberarm.renegade_server_list_v2

import android.content.pm.PackageManager
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dev.cyberarm.renegade_server_list_v2.game_server_hub.Client
import dev.cyberarm.renegade_server_list_v2.game_server_hub.data.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// The do everything singleton :)
object Coordinator {
    val USER_AGENT: String = String.format("Cyberarm's Renegade Server List/%s (cyberarm.dev)", "2.0")
    val NO_PING_MAGIC_NUMBER: Int = 8962

    // thread safe gsh server list
    private val gshClient: Client = Client()
    private val gshClientMutex: Mutex = Mutex()
    private val gshServerList: ArrayList<Server> = ArrayList()
    private val gshServerListMutex: Mutex = Mutex()
    private var isInitialized = false

    fun init(): Boolean {


        return isInitialized
    }

    fun serverList(): ArrayList<Server> {
        return gshServerList
    }

    // does networking stuff
    fun fetchServerList(callback: Runnable): Boolean {
        if (!gshClientMutex.tryLock())
            return false

        thread {
            try {
                if (gshClient.fetchServers()) {

                    // FIXME: thread safety!
                    gshServerList.clear()
                    gshServerList.addAll(gshClient.serverList)

                    callback.run()
                }
            } catch (error: Throwable) {
                throw(error)
            } finally {
                gshClientMutex.unlock();
            }
        }

        return true
    }

    fun server(uuid: String): Server? {
        for (server in serverList()) {
            if (server.id == uuid)
                return server
        }

        return null
    }

    fun serverIcon(game: String): Int {
        return when (game) {
            "apb" -> R.drawable.icon_apb
            "ar" -> R.drawable.icon_ar
            "cwc" -> R.drawable.icon_cwc
            "ecw" -> R.drawable.icon_ecw
            "gz" -> R.drawable.icon_gz
            "ia" -> R.drawable.icon_ia
            "ren" -> R.drawable.icon_ren
            "tsr" -> R.drawable.icon_tsr
            "woa" -> R.drawable.icon_woa
            else -> R.drawable.application_default_image
        }
    }

    // Dynamically swap the primary container view's background image to match the current game
    fun gameBackground(game: String): Int {
        return when (game) {
            "apb" -> R.drawable.background_apb_drawable
            "ar" -> R.drawable.background_ar_drawable
            "cwc" -> R.drawable.background_cwc_drawable
            "ecw" -> R.drawable.background_ecw_drawable
            "gz" -> R.drawable.background_gz_drawable
            "ia" -> R.drawable.background_ia_drawable
            "ren" -> R.drawable.background_ren_drawable
            "tsr" -> R.drawable.background_tsr_drawable
            "woa" -> R.drawable.background_woa_drawable
            else -> R.drawable.application_background_drawable
        }
    }

    fun factionIcon(game: String, team: String): Int {
        val fusion: String = String.format("%s_%s", game, team.lowercase())

        return when (fusion) {
            "apb_soviets" -> R.drawable.faction_apb_soviets
            "apb_allies" -> R.drawable.faction_apb_allies

            "ar_soviets" -> R.drawable.application_default_image
            "ar_allies" -> R.drawable.application_default_image
            "ar_yuri" -> R.drawable.application_default_image

            // cwc, skipping for now :(

            // ecw, 'teamless'

            "gz_soviets" -> R.drawable.application_default_image
            "gz_allies" -> R.drawable.application_default_image

            "ia_nod" -> R.drawable.application_default_image
            "ia_gdi" -> R.drawable.application_default_image

            "ren_nod" -> R.drawable.application_default_image
            "ren_gdi" -> R.drawable.application_default_image

            "tsr_nod" -> R.drawable.application_default_image
            "tsr_gdi" -> R.drawable.application_default_image

            "woa_harkonnen" -> R.drawable.application_default_image
            "woa_atreides" -> R.drawable.application_default_image

            else -> R.drawable.application_default_image
        }
    }

    fun factionColour(game: String, team: String): Long {
        val fusion: String = String.format("%s_%s", game.lowercase(), team.lowercase())

        return when (fusion) {
            "apb_soviets" -> 0
            "apb_allies" -> 0

            "ar_soviets" -> 0
            "ar_allies" -> 0
            "ar_yuri" -> 0

            // cwc, skipping for now :(

            // ecw, 'teamless'

            "gz_soviets" -> 0
            "gz_allies" -> 0

            "ia_nod" -> 0
            "ia_gdi" -> 0

            "ren_nod" -> 0
            "ren_gdi" -> 0

            "tsr_nod" -> 0
            "tsr_gdi" -> 0

            "woa_harkonnen" -> 0
            "woa_atreides" -> 0

            else -> -1
        }
    }

    // Tweak incoming colour for accenting :{
    fun accentColour(colour: Long): Long {
        return colour
    }
}