package dev.cyberarm.renegade_server_list_v2.ui.server_list.server_view

import android.graphics.BitmapFactory
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.transition.Visibility
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import dev.cyberarm.renegade_server_list_v2.Coordinator
import dev.cyberarm.renegade_server_list_v2.R
import dev.cyberarm.renegade_server_list_v2.databinding.FragmentServerViewBinding
import dev.cyberarm.renegade_server_list_v2.game_server_hub.data.Player
import dev.cyberarm.renegade_server_list_v2.game_server_hub.data.Server
import dev.cyberarm.renegade_server_list_v2.game_server_hub.data.Team

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

        activity?.findViewById<ConstraintLayout>(R.id.container)?.background =
            resources.getDrawable(Coordinator.gameBackground(server.game), activity?.theme)

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
            .text = "??:??:??"
        serverInformationContainer.findViewById<TextView>(R.id.server_channel)
            .text = server.channel
        serverInformationContainer.findViewById<TextView>(R.id.server_player_count)
            .text = String.format("%d/%d", server.status.numplayers, server.status.maxplayers)
        serverInformationContainer.findViewById<TextView>(R.id.server_time_left)
            .text = server.status.remaining
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
            .setBackgroundResource(Coordinator.factionIcon(server.game, team.name))
        serverTeamContainer.findViewById<TextView>(R.id.team_name)
            .text = team.name
        serverTeamContainer.findViewById<TextView>(R.id.team_score)
            .text = team.score.toString()
        serverTeamContainer.findViewById<TextView>(R.id.team_kills)
            .text = team.kills.toString()
        serverTeamContainer.findViewById<TextView>(R.id.team_deaths)
            .text = team.deaths.toString()
        serverTeamContainer.findViewById<TextView>(R.id.team_player_count)
            .text = String.format("%d/%d", players.size, server.status.maxplayers)

        val playerRowContainer: LinearLayout = serverTeamContainer.findViewById(R.id.server_team_rows)
        if (players.isEmpty()) {
            playerRowContainer.visibility = GONE
            serverTeamContainer.findViewById<LinearLayout>(R.id.team_separator).visibility = GONE
        }

        var even = true
        val v = TypedValue()
        context?.theme?.resolveAttribute(R.attr.custom_row_item_background, v, true)

        for (player in players) {
            val PlayerRowItem: View = layoutInflater.inflate(R.layout.server_team_card_player_row, null)
            PlayerRowItem.findViewById<TextView>(R.id.player_name)?.text = player.nick
            PlayerRowItem.findViewById<TextView>(R.id.player_score)?.text = player.score.toString()
            PlayerRowItem.findViewById<TextView>(R.id.player_kills)?.text = player.kills.toString()
            PlayerRowItem.findViewById<TextView>(R.id.player_deaths)?.text = player.deaths.toString()
            if (player.ping == Coordinator.NO_PING_MAGIC_NUMBER)
                PlayerRowItem.findViewById<TextView>(R.id.player_ping)?.text = "—"
            else
                PlayerRowItem.findViewById<TextView>(R.id.player_ping)?.text = player.ping.toString()
            PlayerRowItem.findViewById<TextView>(R.id.player_time)?.text = player.time

            if (even) {
                PlayerRowItem.setBackgroundColor(v.data)
            }
            even = !even
            playerRowContainer.addView(PlayerRowItem)
        }

        binding.root.findViewById<LinearLayout>(R.id.server_listing)
            .addView(serverTeamContainer)
    }
}