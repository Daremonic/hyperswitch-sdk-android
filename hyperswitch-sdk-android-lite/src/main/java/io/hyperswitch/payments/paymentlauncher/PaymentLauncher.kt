package io.hyperswitch.payments.paymentlauncher

import android.app.Activity
import io.hyperswitch.model.ConfirmPaymentIntentParams
import org.json.JSONObject


/**
 * API to confirm and handle next actions for PaymentIntent and SetupIntent.
 */
class PaymentLauncher(
    var publishableKey: String?,
    var stripeAccountId: String?,
) {

    /**
     *
     * Confirms and, if necessary, authenticates a PaymentIntent.
     *
     * @param confirmParams confirm payment intent params
     */
    fun confirm(confirmParams: ConfirmPaymentIntentParams) {
        val map = mutableMapOf<String, String?>() //Arguments.createMap()
        map.put("publishableKey", publishableKey)
        map.put("stripeAccountId", stripeAccountId)
        map.put("clientSecret", confirmParams[0].toString())
        map.put("paymentMethodType", "Card")
        map.put("paymentMethodData", confirmParams[1].toString())
//        HyperModule.confirmCard(map)
    }

    object Companion {
        /**
         *
         * Create a [PaymentLauncher] instance with [AppCompatActivity].
         *
         *
         * This API registers an [androidx.activity.result.ActivityResultLauncher] into the [AppCompatActivity],  it needs
         * to be called before the [AppCompatActivity] is created.
         */

        lateinit var onPaymentResult: PaymentResultCallback

        fun create(
            activity: Activity?,
            publishableKey: String?,
            stripeAccountId: String?,
            onPaymentResult: PaymentResultCallback
        ): PaymentLauncher {
            Companion.onPaymentResult = onPaymentResult
            return PaymentLauncher(publishableKey, stripeAccountId)
        }

        fun onPaymentResultCallBack(paymentResult: String) {
            val jsonObject = JSONObject(paymentResult)
            when (val status = jsonObject.getString("status")) {
                "cancelled" -> onPaymentResult.onPaymentResult(PaymentResult.Canceled(status))
                "failed", "requires_payment_method" -> {
                    val throwable = Throwable(jsonObject.getString("message"))
                    throwable.initCause(Throwable(jsonObject.getString("code")))
                    onPaymentResult.onPaymentResult(PaymentResult.Failed(Throwable(throwable)))
                }

                else -> onPaymentResult.onPaymentResult(PaymentResult.Completed(status))
            }
        }
    }
}