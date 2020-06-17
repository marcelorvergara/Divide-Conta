package project.inflabnet.mytest.maps

import android.content.Context

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

class GooglePlacesApi {
    public fun getPlacesJson (
        context: Context,
        lat: Double,
        lng: Double,
        radius: Int,
        type: String,
        s: String
    ) : String{
        var result = ""
        try {
            val urlStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$lat,$lng&radius=$radius&type=$type&key=$s"
            Log.i("URLTeste",urlStr)
            val url = URL(urlStr)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection

            connection.setRequestProperty("Content-Type", "application/json")
            connection.requestMethod = "GET"
            connection.doInput = true

            val br = connection.inputStream.bufferedReader()
            result = br.use {br.readText()}
            connection.disconnect()

        }catch (e: Exception){
            e.printStackTrace()
        }

        return result
    }
}