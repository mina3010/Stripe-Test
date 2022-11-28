package com.qualyup.striptest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.StripeIntent
import kotlinx.android.synthetic.main.activity_card.*


class CardActivity : AppCompatActivity() {

    private lateinit var paymentIntentClientSecret: String
    private lateinit var stripe: Stripe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card)

        stripe = Stripe(this, PaymentConfiguration.getInstance(applicationContext).publishableKey)

        var amount = intent.getDoubleExtra("transactionAmount", 0.0)

        startCheckout(amount)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                if (paymentIntent.status == StripeIntent.Status.Succeeded) {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    displayAlert("Payment succeeded", gson.toJson(paymentIntent))
                } else if (paymentIntent.status == StripeIntent.Status.RequiresPaymentMethod) {
                    displayAlert(
                        "Payment failed",
                        paymentIntent.lastPaymentError?.message.orEmpty()
                    )
                }
            }

            override fun onError(e: Exception) {
                displayAlert("Error", e.toString())
            }
        })
    }

    private fun startCheckout(amountPay: Double) {
        ApiClient().createPaymentIntent(
            amountPay,
            "card",
            "usd",
            completion = { paymentIntentClientSecret, error ->
                run {

                    paymentIntentClientSecret?.let {
                        this.paymentIntentClientSecret = it
                    }
                    error?.let {
                        displayAlert("Failed to load PaymentIntent", "Error: $error")
                    }
                }
            })

        // Confirm the PaymentIntent with the card widget
        payButton.setOnClickListener {
            cardInputWidget.paymentMethodCreateParams?.let { params ->
                val confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                    params,
                    paymentIntentClientSecret
                )
                stripe.confirmPayment(this, confirmParams)
            }
        }
    }

    private fun displayAlert(title: String, message: String) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)

            builder.setPositiveButton("Ok") { _, _ ->
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
            builder.create().show()

        }
    }
}