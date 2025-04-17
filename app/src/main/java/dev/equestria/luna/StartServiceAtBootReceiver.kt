package dev.equestria.luna

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StartServiceAtBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val serviceIntent = Intent(context, ServiceActivity::class.java)
            context.startService(serviceIntent)
        }
    }
}