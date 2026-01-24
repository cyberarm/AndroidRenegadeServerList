package dev.cyberarm.renegade_server_list

import dev.cyberarm.renegade_server_list.game_server_hub.Client
import dev.cyberarm.renegade_server_list.game_server_hub.data.Server
import kotlinx.coroutines.sync.Mutex
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

// The do everything singleton :)
object Coordinator {
    val USER_AGENT: String = String.format("Cyberarm's Renegade Server List/%s (cyberarm.dev)", "2.0")
    val NO_PING_MAGIC_NUMBER: Int = 8962
    val BROADCAST_PORT: Int = 46753

    // thread safe gsh server list
    private val gshClient: Client = Client()
    private val gshClientMutex: Mutex = Mutex()
    private val gshServerList: ArrayList<Server> = ArrayList()
    private val gshServerListMutex: Mutex = Mutex()
    private val broadcastReceiverSocket: DatagramSocket = DatagramSocket(BROADCAST_PORT, InetAddress.getByName("0.0.0.0"))
    private var isInitialized = false

    enum class SpecialTeams(val id: Int) {
        SPECTATOR(-4),
        MUTANT(-3),
        NEUTRAL(-2),
        UNTEAMED(-1)
    }

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

    private fun renegadeTimespan(from: Date, to: Date): String {
        val difference = from.time - to.time
        val hours = difference / (1000 * 60 * 60)
        val minutes = difference / (1000 * 60) % 60
        val seconds = difference / 1000 % 60

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun serverTimeElapsed(server: Server): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSz", Locale.US)
        val startTime = format.parse(server.status.started)

        if (startTime == null)
            return "—"

        return renegadeTimespan(Date(), startTime)
    }

    fun serverTimeRemaining(server: Server): String {
        if (server.status.estimatedEndTime == null)
            return "—"

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSz", Locale.US)
        val currentTime = Date()
        val endTime = format.parse(server.status.estimatedEndTime)

        if (endTime == null)
            return server.status.remaining

        // fix bad estimate
        if (currentTime.time - endTime.time < 0)
            return server.status.remaining

        return renegadeTimespan(currentTime, endTime)
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

    fun factionIcon(game: String, team: String, id: Int): Int {
        // handle special teams specially
        if (id < 0) {
            return when (id) {
                SpecialTeams.SPECTATOR.id -> R.drawable.application_default_image
                SpecialTeams.MUTANT.id -> R.drawable.application_default_image
                SpecialTeams.NEUTRAL.id -> R.drawable.application_default_image
                SpecialTeams.UNTEAMED.id -> R.drawable.application_default_image

                else -> R.drawable.application_default_image
            }
        }

        val fusion: String = String.format("%s_%s", game, team.lowercase())

        return when (fusion) {
            "apb_soviets" -> R.drawable.faction_apb_soviets
            "apb_allies" -> R.drawable.faction_apb_allies

            "ar_soviets" -> R.drawable.faction_ar_soviets
            "ar_allies" -> R.drawable.faction_ar_allies
            "ar_yuri" -> R.drawable.application_default_image

            // cwc, skipping for now :(

            "ecw_nod" -> R.drawable.faction_ecw_nod
            "ecw_gdi" -> R.drawable.faction_ecw_gdi

            "gz_nod" -> R.drawable.faction_gz_nod
            "gz_gdi" -> R.drawable.faction_gz_gdi

            "ia_nod" -> R.drawable.faction_ia_nod
            "ia_gdi" -> R.drawable.faction_ia_gdi

            "ren_nod" -> R.drawable.faction_ren_nod
            "ren_gdi" -> R.drawable.faction_ren_gdi

            "tsr_nod" -> R.drawable.faction_tsr_nod
            "tsr_gdi" -> R.drawable.faction_tsr_gdi

            "woa_harkonnen" -> R.drawable.faction_woa_harkonnen
            "woa_atreides" -> R.drawable.faction_woa_atreides

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