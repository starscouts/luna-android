package dev.equestria.luna

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class HTTPRequest {
    companion object {
        fun request(
            url: String,
            context: Context,
            positiveCallback: (JSONObject) -> Unit = { },
            negativeCallback: (VolleyError) -> Unit = { },
            data: JSONObject = JSONObject(),
            method: Int = Request.Method.GET
        ) {
            val volleyQueue = Volley.newRequestQueue(context)

            val jsonObjectRequest = JsonObjectRequest(method, url, data,

                { response ->
                    positiveCallback(response)
                },

                { error ->
                    error.localizedMessage?.let { Log.e("HTTPRequest", it) }
                    negativeCallback(error)
                })

            volleyQueue.add(jsonObjectRequest)
        }
    }
}