package dev.cyberarm.renegade_server_list.ui.server_list.server_view

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.cyberarm.renegade_server_list.Coordinator
import dev.cyberarm.renegade_server_list.R
import dev.cyberarm.renegade_server_list.databinding.FragmentServerViewBinding
import dev.cyberarm.renegade_server_list.game_server_hub.data.Player
import dev.cyberarm.renegade_server_list.game_server_hub.data.Server
import dev.cyberarm.renegade_server_list.game_server_hub.data.Team
import java.util.Locale

class ServerViewFragment : Fragment() {

    private var _binding: FragmentServerViewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val serverViewViewModel =
            ViewModelProvider(this).get(ServerViewViewModel::class.java)

        _binding = FragmentServerViewBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val serverUUID = arguments?.getString("server_uuid")
        // FIXME: treat this as an error and do something helpful
        if (serverUUID == null)
            return root

        val server = Coordinator.server(serverUUID)
        // FIXME: treat this as an error and do something helpful
        if (server == null)
            return root

        // set background image
        activity?.findViewById<ConstraintLayout>(R.id.container)?.background =
            resources.getDrawable(Coordinator.gameBackground(server.game), activity?.theme)

        // set title
        (requireActivity() as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar)?.title = server.status.name

        populateServerInformationCard(server)
        populateServerTeamCards(server)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun populateServerInformationCard(server: Server) {
        val serverInformationContainer: View = layoutInflater.inflate(R.layout.server_information_card, null)
        serverInformationContainer.findViewById<ImageView>(R.id.server_app_icon)
            .setBackgroundResource(Coordinator.serverIcon(server.game))
        serverInformationContainer.findViewById<TextView>(R.id.server_current_map)
            .text = server.status.map
        if (server.status.nextmap.isEmpty())
            serverInformationContainer.findViewById<TextView>(R.id.server_next_map)
                .text = "—"
        else
            serverInformationContainer.findViewById<TextView>(R.id.server_next_map)
                .text = server.status.nextmap
        serverInformationContainer.findViewById<TextView>(R.id.server_region)
            .text = server.region
        serverInformationContainer.findViewById<TextView>(R.id.server_ping)
            .text = "—"
        serverInformationContainer.findViewById<TextView>(R.id.server_time_elapsed)
            .text = Coordinator.serverTimeElapsed(server)
        serverInformationContainer.findViewById<TextView>(R.id.server_channel)
            .text = server.channel
        serverInformationContainer.findViewById<TextView>(R.id.server_player_count)
            .text = String.format(Locale.US, "%d/%d", server.status.numplayers, server.status.maxplayers)
        serverInformationContainer.findViewById<TextView>(R.id.server_time_left)
            .text = Coordinator.serverTimeRemaining(server)
        serverInformationContainer.findViewById<TextView>(R.id.server_version)
            .text = server.version

        binding.root.findViewById<LinearLayout>(R.id.server_listing).addView(serverInformationContainer)
    }

    fun populateServerTeamCards(server: Server) {
        for (team in server.status.teams) {
            // FIXME: Handle all the teams :}
            if (team.id > 1)
                return

            populateServerTeamCard(server, team)
        }
    }

    fun populateServerTeamCard(server: Server, team: Team) {
        val players: List<Player> = server.status.players.filter { player -> player.team == team.id }
            .sortedBy { it.score }
            .reversed()

        val serverTeamContainer: View = layoutInflater.inflate(R.layout.server_team_card, null)
        serverTeamContainer.findViewById<ImageView>(R.id.team_faction_icon)
            .setBackgroundResource(Coordinator.factionIcon(server.game, team.name, team.id))
        serverTeamContainer.findViewById<TextView>(R.id.team_name)
            .text = team.name
        serverTeamContainer.findViewById<TextView>(R.id.team_score)
            .text = team.score.toString()
        serverTeamContainer.findViewById<TextView>(R.id.team_kills)
            .text = team.kills.toString()
        serverTeamContainer.findViewById<TextView>(R.id.team_deaths)
            .text = team.deaths.toString()
        serverTeamContainer.findViewById<TextView>(R.id.team_player_count)
            .text = String.format(Locale.US, "%d/%d", players.size, server.status.maxplayers)

        val playerRowContainer: LinearLayout = serverTeamContainer.findViewById(R.id.server_team_rows)
        if (players.isEmpty()) {
            playerRowContainer.visibility = View.GONE
            serverTeamContainer.findViewById<LinearLayout>(R.id.team_separator).visibility = View.GONE
        }

        var even = true
        val v = TypedValue()
        context?.theme?.resolveAttribute(R.attr.custom_row_item_background, v, true)

        for (player in players) {
            val playerRowItem: View = layoutInflater.inflate(R.layout.server_team_card_player_row, null)
            playerRowItem.findViewById<TextView>(R.id.player_name)?.text = player.nick
            playerRowItem.findViewById<TextView>(R.id.player_score)?.text = player.score.toString()
            playerRowItem.findViewById<TextView>(R.id.player_kills)?.text = player.kills.toString()
            playerRowItem.findViewById<TextView>(R.id.player_deaths)?.text = player.deaths.toString()
            if (player.ping == Coordinator.NO_PING_MAGIC_NUMBER)
                playerRowItem.findViewById<TextView>(R.id.player_ping)?.text = "—"
            else
                playerRowItem.findViewById<TextView>(R.id.player_ping)?.text = player.ping.toString()
            playerRowItem.findViewById<TextView>(R.id.player_time)?.text = player.time

            if (even) {
                playerRowItem.setBackgroundColor(v.data)
            }
            even = !even
            playerRowContainer.addView(playerRowItem)
        }

        binding.root.findViewById<LinearLayout>(R.id.server_listing)
            .addView(serverTeamContainer)
    }
}