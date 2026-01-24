package dev.cyberarm.renegade_server_list.ui.launcher_remote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.cyberarm.renegade_server_list.databinding.FragmentLauncherRemoteBinding

class LauncherRemoteFragment : Fragment() {

    private var _binding: FragmentLauncherRemoteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val launcherRemoteViewModel =
            ViewModelProvider(this).get(LauncherRemoteViewModel::class.java)

        _binding = FragmentLauncherRemoteBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}