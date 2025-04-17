package dev.equestria.luna

import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class SystemProperties {
    private val GETPROP_EXECUTABLE_PATH = "/system/bin/getprop"
    private val TAG = "MyApp"

    fun read(propName: String): String {
        var process: Process? = null
        var bufferedReader: BufferedReader? = null
        return try {
            process = ProcessBuilder().command(GETPROP_EXECUTABLE_PATH, propName)
                .redirectErrorStream(true).start()
            bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line = bufferedReader.readLine()
            if (line == null) {
                line = "" //prop not set
            }
            Log.i(TAG, "read System Property: $propName=$line")
            line
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read System Property $propName", e)
            ""
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (e: IOException) {
                }
            }
            process?.destroy()
        }
    }
}