package dev.cyberarm.renegade_server_list.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.cyberarm.renegade_server_list.Coordinator
import dev.cyberarm.renegade_server_list.R
import dev.cyberarm.renegade_server_list.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        root.findViewById<EditText>(R.id.input_ingame_name)
            .setText(Coordinator.applicationSettings.inGameName)
        root.findViewById<ToggleButton>(R.id.input_auto_refresh_server_list)
            .isChecked = Coordinator.applicationSettings.serverListAutoRefreshEnabled
        root.findViewById<EditText>(R.id.input_auto_refresh_interval)
            .setText(Coordinator.applicationSettings.serverListRefreshIntervalInMinutes.toString())
        root.findViewById<ToggleButton>(R.id.input_auto_refresh_metered_connection)
            .isChecked = Coordinator.applicationSettings.serverListRefreshOnMeteredConnection

        root.findViewById<EditText>(R.id.input_notify_player_count)
            .setText(Coordinator.applicationSettings.notifyServerSettings.notifyPlayerCount.toString())
        root.findViewById<EditText>(R.id.input_notify_map_names)
            .setText(Coordinator.applicationSettings.notifyServerSettings.notifyMapNames.joinToString(", "))
        root.findViewById<EditText>(R.id.input_notify_usernames)
            .setText(Coordinator.applicationSettings.notifyServerSettings.notifyUsernames.joinToString(", "))
        root.findViewById<ToggleButton>(R.id.input_notify_multiple_conditions)
            .isChecked = Coordinator.applicationSettings.notifyServerSettings.notifyRequireMultipleConditions

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}