package dev.cyberarm.android_renegade_server_list.library;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;

public class AppOnBootReceiver extends BroadcastReceiver {
    private static final String TAG = "AppOnBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (!AppSync.appInitialized) {
                AppSync.initialize(context.getFilesDir());
            }

            if (AppSync.settings.serviceAutoStartAtBoot) {
                Log.i(TAG, "Auto starting worker...");

                OneTimeWorkRequest.from(RenegadeServerListWorker.class);
            } else {
                Log.i(TAG, "Auto starting Worker is disabled.");
            }
        }
    }
}
