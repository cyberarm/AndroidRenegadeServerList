package dev.cyberarm.renegade_server_list_v2

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dev.cyberarm.renegade_server_list_v2.databinding.ActivityMainBinding
import dev.cyberarm.renegade_server_list_v2.game_server_hub.Client
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // "New" android versions appear to require this?

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = binding.toolbar
        val navView: BottomNavigationView = binding.navView

        // "FIX" `enableEdgeToEdge` causing app toolbar to render behind system status bar
        // BEGIN RANT: and people wonder why solo dev apps don't get updated.
        // It's because we don't have time to release weekly updates
        // for 'stable' apps and when we do have time we practically have to rebuild
        // our apps from scratch to handle all the changes to the android ecosystem
        // in the last year. END RANT
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view. This solution sets
            // only the bottom, left, and right dimensions, but you can apply whichever
            // insets are appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            findViewById<Toolbar>(R.id.toolbar).updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                bottomMargin = insets.bottom
//                topMargin = insets.top // NOTE: Old man yells at clouds meme goes here... :|
                rightMargin = insets.right
            }

            toolbar.setPadding(0, insets.top, 0, 0)
            navView.setPadding(0, 0, 0, insets.bottom)

            WindowInsetsCompat.CONSUMED
        }

        // FIXME: Doesn't work?
        toolbar.setNavigationIcon(R.drawable.kenney_arrow_left)

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_server_list, R.id.navigation_launcher_remote, R.id.navigation_settings
            )
        )

        toolbar.setupWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        thread() {
            val gshClient = Client()
            gshClient.fetchServers()
        }
    }

    // Uh... why isn't this done by default... come on google, include complete templates...
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }
}