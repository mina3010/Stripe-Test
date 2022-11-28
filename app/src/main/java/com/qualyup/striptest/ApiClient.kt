package com.qualyup.striptest

import com.qualyup.striptest.Constants.backEndUrl
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ApiClient {
    private val httpClient = OkHttpClient()

    fun createPaymentIntent(
        amount: Double,
        paymentMethodType: String,
        currency: String,
        completion: (paymentIntentClientSecret: String?, error: String?) -> Unit
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestJson = """
            {
            "amount": $amount,
            "currency": "$currency",
            "paymentMethodType": "$paymentMethodType"
            }
        """.trimIndent()
        val body = requestJson.toRequestBody(mediaType)
        val request = Request.Builder().url("$backEndUrl/create-payment-intent").post(body).build()
        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(null, "$e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val responseJson = responseData?.let { JSONObject(it) } ?: JSONObject()
                    val paymentIntentClientSecret: String = responseJson.getString("clientSecret")
                    completion(paymentIntentClientSecret, null)
                } else {
                    completion(null, "$response")
                }
            }
        })
    }
}