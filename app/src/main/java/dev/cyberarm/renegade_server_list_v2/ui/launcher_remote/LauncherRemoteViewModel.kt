package dev.cyberarm.renegade_server_list_v2.ui.launcher_remote

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LauncherRemoteViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Under Construction"
    }
    val text: LiveData<String> = _text
}