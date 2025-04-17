package dev.equestria.luna

import android.Manifest
import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.core.widget.NestedScrollView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.equestria.luna.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPref: DataViewModel
    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        Log.d("ServiceManager", "Starting check")
        try {
            val manager =
                getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(
                Int.MAX_VALUE
            )) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
        } catch (e: Exception) {
            Log.w("ServiceManager", e)
            return false
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = DataViewModel(DataStoreRepositoryImpl(this))
        DynamicColors.applyToActivitiesIfAvailable(application)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_WIFI_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_WIFI_STATE), 2)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val mChannel = NotificationChannel("service", "Luna background service", NotificationManager.IMPORTANCE_DEFAULT)
        mChannel.description = "Used to run Luna in the background. Disabling this channel won't prevent Luna from running in the background."

        val mChannel2 = NotificationChannel("alerts", "Luna alerts", NotificationManager.IMPORTANCE_DEFAULT)
        mChannel2.description = "Notifications shown when Luna encounters a problem that needs user intervention."

        notificationManager.createNotificationChannel(mChannel)
        notificationManager.createNotificationChannel(mChannel2)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val textView = findViewById<TextView>(R.id.status)

        if (sharedPref.getToken() == null || sharedPref.getToken() == "") {
            textView.text = getString(R.string.status_unpaired)
        } else {
            textView.text = "Luna has been paired with a Cold Haze account and is now running properly."
        }

        binding.fab.setOnClickListener { view ->
            if (sharedPref.getToken() != null && sharedPref.getToken() != "") {
                MaterialAlertDialogBuilder(binding.root.context)
                    .setTitle("Unpair this device?")
                    .setMessage("This device is currently paired with a Cold Haze account, do you want to unpair it?")
                    .setPositiveButton("Unpair") { _, _ ->
                        sharedPref.saveToken("")
                        sharedPref.clearToken()
                        textView.text = getString(R.string.status_unpaired)
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .show()
            } else {
                textView.text = getString(R.string.status_preparing)
                createRequest()
            }
        }

        createTestRequest()
    }

    private fun createRequest() {
        val view = findViewById<TextView>(R.id.status)

        val webSocketListener = WebSocketListener({
            view.text = it
        }, {
            sharedPref.saveToken(it)
        }, { id: Int ->
            return@WebSocketListener getString(id)
        }, { id: Int, args: Array<String> ->
            return@WebSocketListener getString(id, *args)
        })
        val websocketURL = "wss://${getString(R.string.root)}/_PairingServices-WebSocket-EntryPoint/socket"
        webSocket = okHttpClient.newWebSocket(Request.Builder()
            .url(websocketURL)
            .build(), webSocketListener)
    }

    private fun createTestRequest() {
        val view = findViewById<TextView>(R.id.status)

        val webSocketListener = WebSocketListener({
            view.text = it

            if (sharedPref.getToken() == null || sharedPref.getToken() == "") {
                view.text = getString(R.string.status_unpaired)
            } else {
                view.text = "Luna has been paired with a Cold Haze account and is now running properly."
            }
        }, {
            if (!isServiceRunning(ServiceActivity::class.java)) {
                Log.d("ServiceManager", "Starting service as it is not running")
                startForegroundService(Intent(this, ServiceActivity::class.java))
            } else {
                Log.d("ServiceManager", "Service is running, not starting")
            }
        }, { id: Int ->
            return@WebSocketListener getString(id)
        }, { id: Int, args: Array<String> ->
            return@WebSocketListener getString(id, *args)
        }, true)
        val websocketURL = "wss://${getString(R.string.root)}/_PairingServices-WebSocket-EntryPoint/socket"
        webSocket = okHttpClient.newWebSocket(Request.Builder()
            .url(websocketURL)
            .build(), webSocketListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}