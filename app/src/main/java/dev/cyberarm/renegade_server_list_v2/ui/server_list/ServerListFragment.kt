package dev.cyberarm.renegade_server_list_v2.ui.server_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dev.cyberarm.renegade_server_list_v2.Coordinator
import dev.cyberarm.renegade_server_list_v2.R
import dev.cyberarm.renegade_server_list_v2.databinding.FragmentServerListBinding
import dev.cyberarm.renegade_server_list_v2.ui.server_list.server_view.ServerViewFragment

class ServerListFragment : Fragment() {

    private var _binding: FragmentServerListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val serverListViewModel =
            ViewModelProvider(this).get(ServerListViewModel::class.java)

        _binding = FragmentServerListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val refreshLayout = root.findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener {
            Coordinator.fetchServerList {
                activity?.runOnUiThread {
                    populateServers()
                    refreshLayout.isRefreshing = false
                }
            }
        }

        if (Coordinator.serverList().isEmpty()) {
            refreshLayout.isRefreshing = true

            Coordinator.fetchServerList {
                activity?.runOnUiThread {
                    populateServers()
                    refreshLayout.isRefreshing = false
                }
            }
        } else {
            populateServers()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun populateServers() {
        binding.root.findViewById<LinearLayout>(R.id.server_listing).removeAllViews()

        for (server in Coordinator.serverList()) {
            val serverItemContainer: View = layoutInflater.inflate(R.layout.server_item, null)

            serverItemContainer.findViewById<ImageView>(R.id.server_app_icon)
                .setBackgroundResource(Coordinator.serverIcon(server.game))
            serverItemContainer.findViewById<TextView>(R.id.server_name)
                .text = server.status.name
            serverItemContainer.findViewById<TextView>(R.id.server_current_map)
                .text = server.status.map
            serverItemContainer.findViewById<TextView>(R.id.server_player_count)
                .text = String.format("%d/%d", server.status.numplayers, server.status.maxplayers)

            binding.root.findViewById<LinearLayout>(R.id.server_listing)
                .addView(serverItemContainer)

            serverItemContainer.setOnClickListener {
                val bundle = bundleOf("server_uuid" to server.id)
                findNavController().navigate(
                    R.id.action_navigation_server_list_to_serverViewFragment,
                    bundle
                )
            }
        }
    }
}