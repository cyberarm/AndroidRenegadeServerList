package dev.cyberarm.renegade_server_list_v2.ui.server_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBindings
import dev.cyberarm.renegade_server_list_v2.R
import dev.cyberarm.renegade_server_list_v2.databinding.FragmentServerListBinding

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

        val cheese = IntArray(40) { i -> i + i }
        for (value in cheese) {
            Log.i("SERVER LIST FRAGMENT", "appending Server Item Container...")
            val serverItemContainer: View =
                layoutInflater.inflate(R.layout.server_item, null)
            binding.root.findViewById<LinearLayout>(R.id.server_listing).addView(serverItemContainer)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}