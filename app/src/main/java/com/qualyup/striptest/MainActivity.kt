package com.qualyup.striptest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.qualyup.striptest.Constants.backEndUrl
import com.stripe.android.PaymentConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val httpClient = OkHttpClient()
    private lateinit var publishableKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchPublishableKey()
        initComponents()
    }

    private fun initComponents() {
        paymentMethodCard.setOnClickListener {
            if (transactionAmount.text.toString().isNotEmpty()) {
                val intent = Intent(this, CardActivity::class.java)
                intent.putExtra(
                    "transactionAmount",
                    transactionAmount.text.toString().toDouble() * 100
                )
                startActivity(intent)
            } else {
                Toast.makeText(this, "Insert payment amount", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayAlert(title: String, message: String) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder.create().show()
        }
    }

    //Fetch publishable key from server and initialise the Stripe SDK
    private fun fetchPublishableKey() {
        val request = Request.Builder().url("$backEndUrl/config").build()

        httpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                displayAlert("Request failed", "Error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val responseJson = responseData?.let { JSONObject(it) } ?: JSONObject()
                    publishableKey = responseJson.getString("publishableKey")

                    PaymentConfiguration.init(applicationContext, publishableKey)
                } else {
                    displayAlert("Request failed", "Error: $response")
                }
            }
        })
    }
}