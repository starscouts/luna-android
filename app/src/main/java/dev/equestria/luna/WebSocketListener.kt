package dev.equestria.luna

import android.os.Build
import android.util.Log
import android.widget.TextView
import com.google.gson.Gson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.Objects

class WebSocketListener(
    private val changeText: (text: String) -> Unit,
    private val callback: (token: String) -> Unit,
    private val getString: (id: Int) -> String,
    private val getStringWithArgs: (id: Int, args: Array<String>) -> String,
    private val stopAfterConnect: Boolean = false
): WebSocketListener() {

    private var completed: Boolean = false
    private val tag = "WebSocket"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        changeText("Connected to the pairing server")
        Log.d(tag, "onOpen:")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        Log.d(tag, "onMessage: $text")

        val data = JSONObject(text)

        if (data.getString("type") == "waiting") {
            changeText("Use the following pairing code with Cold Haze: ${data.getString("code")}")

            if (stopAfterConnect) {
                webSocket.close(1000, "Was a connection test")
                callback("")
            }
        } else if (data.getString("type") == "init") {
            val sendData = object {
                val type = "init"
                val name = "Luna Mobile (Android)"
            }

            val sendText = Gson().toJson(sendData)
            Log.d(tag, "sending: $sendText")
            webSocket.send(sendText)
        } else if (data.getString("type") == "preflight") {
            changeText("${data.getJSONObject("identity").getString("name")} from ${data.getJSONObject("identity").getString("platform")} is trying to pair")
        } else if (data.getString("type") == "confirm") {
            completed = true
            webSocket.close(1000, "Done")
            changeText("Pairing has been completed, Luna is now sending data.")
            callback(data.getString("token"))
        } else if (data.getString("type") == "reject") {
            completed = true
            webSocket.close(1000, "Done")
            changeText("Your other device has rejected the pairing request, please try again.")
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        if (!completed) changeText("Connection is closing...")
        Log.d(tag, "onClosing: $code $reason")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
        if (!completed) changeText("Connection is now closed")
        Log.d(tag, "onClosed: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(tag, "onFailure: ${t.message} $response")
        changeText("Pairing failed with an error: ${t.message.toString()}")
        super.onFailure(webSocket, t, response)

        if (stopAfterConnect) {
            webSocket.close(1000, "Was a connection test")
            callback("")
        }
    }
}