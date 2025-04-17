package dev.equestria.luna

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.opengl.GLES10
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.android.volley.Request
import com.google.gson.Gson
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.RandomAccessFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs


class ServiceActivity : Service() {
    private var authenticated: Boolean = true
    private lateinit var deviceId: String
    private lateinit var context: Context
    private lateinit var data: DataViewModel
    private lateinit var bm: BatteryManager
    private lateinit var dm: DisplayManager
    private lateinit var ifilter: IntentFilter
    private lateinit var bs: Intent

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun gatherData(): Any {
        return object {
            val _session: String? = data.getToken()
            val luna_version: String = BuildConfig.VERSION_NAME.split("-")[0]
            val dsb = object {
                val platform: String = "android"
                val version: Int = BuildConfig.VERSION_CODE
            }
            val top_sites = null
            val host: String = deviceId
            val os: String = "Android " + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Build.VERSION.RELEASE_OR_CODENAME else Build.VERSION.RELEASE
            val kernel: String = "Linux " + System.getProperty("os.version") + " " + System.getProperty("os.arch")
            val serial: String = Build.BRAND + " " + Build.MODEL + " (" + deviceId + ")"
            val serial_source: String = "software"
            val remote_control: Boolean = false
            val date: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            val screens = arrayOf(object {
                val id: String = "0"
                val gid: String = "0"
                val name: String = "Screen"
            })
            val windows = arrayOf<String>()
            val cpu = object {
                val manufacturer: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MANUFACTURER else Build.MANUFACTURER
                val brand: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else Build.BRAND
                val family: String = ""
                val stepping: String = ""
                val revision: String = ""
                val voltage: String = ""
                val speed: Float = getCPUFrequency().toFloat() / 1000000f
                val speedMin: Float = speed
                val speedMax: Float = speed
                val governor: String = getCPUGovernor()
                val cores: Int = Runtime.getRuntime().availableProcessors()
                val physicalCores: Int = cores
                val performanceCores: Int = 0
                val efficiencyCores: Int = 0
                val processors: Int = 1
                val socket: String = ""
                val flags: Array<String> = Build.SUPPORTED_ABIS
                val virtualization = null
                val cache = null
            }
            val temperature = object {
                val main: Float = getCPUTemp()?.toFloat() ?: 0f
                val cores = arrayOf<String>()
                val max = null
                val socket = arrayOf<String>()
                val chipset = null
            }
            val ram = object {
                val total: Double = abs(MemoryInfo.MemTotal * 1024.0)
                val free: Double = abs(MemoryInfo.MemFree * 1024.0)
                val used: Double = abs(total - free)
                val active: Double = abs(MemoryInfo.Active * 1024.0)
                val available: Double = abs(MemoryInfo.MemAvailable * 1024.0)
                val buffers: Double = abs(MemoryInfo.Buffers * 1024.0)
                val cached: Double = abs(MemoryInfo.Cached * 1024.0)
                val slab: Double = abs(MemoryInfo.Slab * 1024.0)
                val buffcache: Double = abs((MemoryInfo.Buffers + MemoryInfo.Cached) * 1024.0)
                val swaptotal: Double = abs(MemoryInfo.SwapTotal * 1024.0)
                val swapfree: Double = abs(MemoryInfo.SwapFree * 1024.0)
                val swapused: Double = abs(swaptotal - swapfree)
            }
            val ram_chips = arrayOf(object {
                val size: Double = sizeToBytes(getProperty("ro.boot.ddr_size"))
                val bank: String = ""
                val type: String = try { getProperty("ro.boot.hardware.ddr").split(",")[2] } catch (e: Exception) { "" }
                val ecc: Boolean = false
                val clockSpeed: Int = 0
                val formFactor: String = ""
                val manufacturer: String = try { getProperty("ro.boot.hardware.ddr").split(",")[1] } catch (e: Exception) { "" }
                val partNum: String = ""
                val serialNum: String = ""
                val voltageConfigured = null
                val voltageMin = null
                val voltageMax = null
            })
            val battery = object {
                val hasBattery: Boolean = true
                val cycleCount: Int = if (Build.VERSION.SDK_INT >= 34) bs?.getIntExtra("cycle_count", 0)!! else 0
                val isCharging: Boolean = bm.isCharging
                val designedCapacity: Int = 0
                val maxCapacity: Float = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toFloat() / 1000f
                val currentCapacity: Float = (bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER).toFloat() * (bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).toFloat() / 100f)) / 1000f
                val voltage: Float = bs.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0).toFloat() / 1000f
                val capacityUnit: String = "mWh"
                val percent: Int = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                val timeRemaining = null
                val acConnected: Boolean = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_NOT_CHARGING || bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_CHARGING || bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) == BatteryManager.BATTERY_STATUS_FULL
                val type: String = bs?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)!!
                val model: String = ""
                val manufacturer: String = ""
                val serial: String = ""
            }
            val os_info = object {
                val platform: String = "android"
                val distro: String = "Android"
                val release: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Build.VERSION.RELEASE_OR_CODENAME else Build.VERSION.RELEASE
                val codename: String = Build.VERSION.CODENAME
                val kernel: String? = System.getProperty("os.version")
                val arch: String? = System.getProperty("os.arch")
                val hostname: String = deviceId
                val fqdn: String = hostname
                val codepage = null
                val logofile = "android"
                val serial: String = deviceId
                val build: String = Build.DISPLAY
                val servicepack: String = ""
                val uefi = null
            }
            val gpu = object {
                val controllers = arrayOf<String>()
                val displays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) arrayOf(object {
                    val vendor = Build.MANUFACTURER
                    val vendorId = ""
                    val model = ""
                    val productionYear = "0"
                    val serial = ""
                    val displayId = 0
                    val main = true
                    val builtin = true
                    val connection = ""
                    val sizeX: Float = resources.displayMetrics.widthPixels / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
                    val sizeY = resources.displayMetrics.heightPixels / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
                    val pixelDepth = null
                    val resolutionX: Int = resources.displayMetrics.widthPixels
                    val resolutionY: Int = resources.displayMetrics.heightPixels
                    val currentResX: Int = resolutionX
                    val currentResY: Int = resolutionY
                    val positionX: Int = 0
                    val positionY: Int = 0
                    val currentRefreshRate: Float = dm.displays[0].refreshRate
                }) else arrayOf<String>()
            }
            val uuid = object {
                val os = deviceId
                val hardware = deviceId
                val macs = arrayOf(getMacAddress())
            }
            val versions = object {}
            val users = arrayOf<String>()
            val filesystems = getFilesystems()
            val fs_stats = null
            val usb = arrayOf<String>()
            val processess = arrayOf<String>()
            val audio = null
            val network = null
            val connections = null
        }
    }

    fun getFilesystems() {
        val list = mutableListOf<Any>()

        fun executeCommand(command: String): String {
            val process = Runtime.getRuntime().exec(command)
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                output.append(line).append('\n')
            }
            return output.toString().trim()
        }

        val dfOutput: String = executeCommand("df")

        val lines: List<String> = dfOutput.split("\n")
        var index = 0

        for (line in lines) {
            if (index < 1) {
                index++
                continue
            } else {
                index++
            }

            val columns: List<String> = line.trim().split("\\s+".toRegex())

            val filesystem = columns[0]
            val size = columns[1]
            val used = columns[2]
            val available = columns[3]
            val usedPercentage = columns[4]
            val mountPoint = columns[5]

            list.add(object {
                val fs: String = filesystem
                val type = ""
                val size = size.toDouble() * 1024.0
                val used = used.toDouble() * 1024.0
                val available = available.toDouble() * 1024.0
                val use = usedPercentage.split("%")[0].toInt()
                val mount = mountPoint
            })
        }
    }

    fun getMacAddress(): String {
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo: WifiInfo? = wifiManager.connectionInfo
            val macAddress: String? = wifiInfo?.macAddress

            return macAddress ?: ""
        } else {
            return ""
        }
    }

    fun sizeToBytes(size: String): Double {
        return try {
            val regex = """(\d+)(.*)""".toRegex()
            val number: Double = regex.replace(size, "$1").toDouble()
            val prefix: Char = regex.replace(size, "$2").toCharArray()[0].lowercaseChar()
            val multiplier: Double = when (prefix) {
                'k' -> 1024.0
                'm' -> 1048576.0
                'g' -> 1073741824.0
                't' -> 1099511627776.0
                else -> 0.0
            }
            number * multiplier
        } catch (e: Exception) {
            0.0
        }
    }

    fun getProperty(name: String): String {
        val process = Runtime.getRuntime().exec("getprop $name")
        process.waitFor()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        return reader.readLine()
    }

    fun getCPUTemp(): String? {
        val process: Process
        val reader: BufferedReader
        val line: String?
        var t: String? = null
        var temp = 0f
        try {
            process = Runtime.getRuntime().exec("cat /sys/class/thermal/thermal_zone0/temp")
            process.waitFor()
            reader = BufferedReader(InputStreamReader(process.inputStream))
            line = reader.readLine()
            if (line != null) {
                temp = line.toFloat()
            }
            reader.close()
            process.destroy()
            if (temp.toInt() != 0) {
                if (temp.toInt() > 10000) {
                    temp = temp / 1000
                } else if (temp.toInt() > 1000) {
                    temp = temp / 100
                } else if (temp.toInt() > 100) {
                    temp = temp / 10
                }
            } else t = "0.0"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return t
    }

    fun getCPUGovernor(): String {
        var cpuGovernor = ""
        val reader = RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor", "r")
        cpuGovernor = reader.readLine()
        reader.close()
        return cpuGovernor
    }

    fun getCPUFrequency(): String {
        var cpuMaxFreq = ""
        val reader = RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r")
        cpuMaxFreq = reader.readLine()
        reader.close()
        return cpuMaxFreq
    }

    fun objectToJSON(obj: Any): JSONObject {
        return JSONObject(Gson().toJson(obj))
    }

    fun sendHeartbeat() {
        HTTPRequest.request("https://${getString(R.string.root)}/api/session", applicationContext, {
            Log.d("LunaService", it.toString())

            if (it.get("name") is String) {
                authenticated = true
            } else if (authenticated) {
                authenticated = false
                data.saveToken("")
                data.clearToken()

                val pendingIntent: PendingIntent =
                    Intent(context, MainActivity::class.java).let { notificationIntent ->
                        PendingIntent.getActivity(context, 0, notificationIntent,
                            PendingIntent.FLAG_IMMUTABLE)
                    }
                val notification: Notification = Notification.Builder(context, "alerts")
                    .setContentTitle("This device has been unpaired")
                    .setContentText("Luna cannot monitor this device as it has been unpaired from your Cold Haze account. Click here to pair it again.")
                    .setSmallIcon(R.drawable.ic_warning)
                    .setContentIntent(pendingIntent)
                    .build()
                with (NotificationManagerCompat.from(context)) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        notify(125, notification)
                    }
                }
            }

            if (authenticated) {
                HTTPRequest.request("https://${getString(R.string.root)}/api/computer?type=heartbeat&json", applicationContext, {
                    Log.d("LunaService", it.toString())
                }, { }, objectToJSON(object {
                    val _session: String? = data.getToken()
                    val host: String = deviceId
                }), Request.Method.POST)
            }
        }, { }, objectToJSON(object {
            val _session: String? = data.getToken()
        }), Request.Method.POST)
    }

    fun sendScreenshot() {
        val tokenData = data

        val wallpaperManager: WallpaperManager = WallpaperManager.getInstance(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("LunaService", "Sending screenshot")
            val wallpaperDrawable: Drawable = wallpaperManager.drawable

            val bitmap = wallpaperDrawable.toBitmap(wallpaperDrawable.intrinsicWidth / 4, wallpaperDrawable.intrinsicHeight / 4)
            val byteStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream)
            val byteArray = byteStream.toByteArray()
            val baseString: String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            HTTPRequest.request("https://${getString(R.string.root)}/api/computer?type=screenshot&json", applicationContext, {
                Log.d("LunaService", it.toString())
            }, { }, objectToJSON(object {
                val _session: String? = tokenData.getToken()
                val host: String = deviceId
                val id: Int = 0
                val data: String = baseString
            }), Request.Method.POST)

            return
        } else {
            Log.e("LunaService", "Permission to screenshot denied")
        }
    }

    fun sendData() {
        HTTPRequest.request("https://${getString(R.string.root)}/api/computer?type=data&json", applicationContext, {
            Log.d("LunaService", it.toString())
        }, { }, objectToJSON(gatherData()), Request.Method.POST)
    }

    @SuppressLint("HardwareIds")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        context = this
        deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

        bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        dm = context.getSystemService(DISPLAY_SERVICE) as DisplayManager
        ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        bs = context.registerReceiver(null, ifilter)!!

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE)
            }
        data = DataViewModel(DataStoreRepositoryImpl(this))

        val notification: Notification = Notification.Builder(this, "service")
            .setContentTitle("Luna background service")
            .setContentText("Luna is currently monitoring this device")
            .setSmallIcon(R.drawable.ic_service)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(123, notification)

        val handler = Handler()
        val delay: Long = 5000
        val delay2: Long = 60000
        val delay3: Long = 600000

        if (authenticated) {
            HTTPRequest.request("https://${getString(R.string.root)}/api/rename", applicationContext, { }, { }, objectToJSON(object {
                val _session: String? = data.getToken()
                val name: String = getString(R.string.app_session_name, BuildConfig.VERSION_NAME.split("-")[0], BuildConfig.VERSION_CODE.toString(), Build.MODEL)
            }), Request.Method.POST)
        }

        handler.postDelayed(object : Runnable {
            override fun run() {
                sendHeartbeat()

                handler.postDelayed(this, delay)
            }
        }, delay)

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (authenticated) {
                    sendScreenshot()
                }

                handler.postDelayed(this, delay2)
            }
        }, delay2)

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (authenticated) {
                    sendData()
                }

                handler.postDelayed(this, delay3)
            }
        }, delay3)

        sendHeartbeat()
        sendScreenshot()
        sendData()

        return START_STICKY
    }
}