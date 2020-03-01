package br.com.tiker.scene.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import br.com.tiker.R
import br.com.tiker.payment.PaymentsUtil
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import kotlinx.android.synthetic.main.fragment_become_premium.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class BecomePremiumFragment : Fragment() {

    companion object {
        /**
         * Arbitrarily-picked constant integer you define to track a request for payment data activity.
         *
         * @value #LOAD_PAYMENT_DATA_REQUEST_CODE
         */
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    }

    private val paymentClient by lazy {
        PaymentsUtil.createPaymentsClient(activity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_become_premium, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        gPayButton?.setOnClickListener {
            requestPayment(it)
        }
    }

    private fun initUi() {
        possiblyShowGooglePayButton()
    }

    /**
     * Determine the viewer's ability to pay with a payment method supported by your app and display a
     * Google Pay payment button.
     *
     * @see [](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient.html.isReadyToPay
    ) */
    private fun possiblyShowGooglePayButton() {
        val isReadyToPayJson: Optional<JSONObject> = PaymentsUtil.getIsReadyToPayRequest()
        if (!isReadyToPayJson.isPresent) {
            return
        }
        val request =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString())
                    ?: return
            } else {
                TODO("VERSION.SDK_INT < N")
            }
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val task: Task<Boolean> = paymentClient.isReadyToPay(request)
        task.addOnCompleteListener {
            if (it.isSuccessful && task.result != null) {
                setGooglePayAvailable(task.result!!)
            } else {
                Log.w("isReadyToPay failed", task.exception)
            }
        }
    }

    /**
     * If isReadyToPay returned `true`, show the button and hide the "checking" text. Otherwise,
     * notify the user that Google Pay is not available. Please adjust to fit in with your current
     * user flow. You are not required to explicitly let the user know if isReadyToPay returns `false`.
     *
     * @param available isReadyToPay API response.
     */
    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
//            mGooglePayStatusText.setVisibility(View.GONE)
            gPayButton.visibility = View.VISIBLE
        } else {
            gPayButton.visibility = View.GONE
//            mGooglePayStatusText.setText(R.string.googlepay_status_unavailable)
        }
    }

    // This method is called when the Pay with Google button is clicked.
    private fun requestPayment(view: View?) { // Disables the button to prevent multiple clicks.
        view?.isClickable = false
        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val price =
            PaymentsUtil.microsToString(2)
        // TransactionInfo transaction = PaymentsUtil.createTransaction(price);
        val paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(price)
        if (!paymentDataRequestJson.isPresent) {
            return
        }
        val request =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString())
            } else {
                TODO("VERSION.SDK_INT < N")
            }
        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        if (request != null) {
            AutoResolveHelper.resolveTask(
                paymentClient.loadPaymentData(request), activity!!, LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     * @see [Getting a result
     * from an Activity](https://developer.android.com/training/basics/intents/result)
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val paymentData = PaymentData.getFromIntent(data!!)
                        handlePaymentSuccess(paymentData!!)
                    }
                    Activity.RESULT_CANCELED -> {
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        val status: Status? = AutoResolveHelper.getStatusFromIntent(data)
                        handleError(status!!.getStatusCode())
                    }
                    else -> {
                    }
                }
                // Re-enables the Google Pay payment button.
                gPayButton?.isClickable = true
            }
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [Payment
     * Data](https://developers.google.com/pay/api/android/reference/object.PaymentData)
     */
    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return
        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        val paymentMethodData: JSONObject
        try {
            paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            // If the gateway is set to "example", no payment information is returned - instead, the
// token will only consist of "examplePaymentMethodToken".
            if ((paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("type")
                        == "PAYMENT_GATEWAY") && (paymentMethodData
                    .getJSONObject("tokenizationData")
                    .getString("token")
                        == "examplePaymentMethodToken")
            ) {
                val alertDialog = AlertDialog.Builder(activity)
                    .setTitle("Warning")
                    .setMessage(
                        "Gateway name set to \"example\" - please modify "
                                + "Constants.java and replace it with your own gateway."
                    )
                    .setPositiveButton("OK", null)
                    .create()
                alertDialog.show()
            }
            val billingName =
                paymentMethodData.getJSONObject("info").getJSONObject("billingAddress")
                    .getString("name")
            Log.d("BillingName", billingName)
            Toast.makeText(
                activity,
                billingName,
                Toast.LENGTH_LONG
            )
                .show()
            // Logging token string.
            Log.d(
                "GooglePaymentToken",
                paymentMethodData.getJSONObject("tokenizationData").getString("token")
            )
        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $e")
            return
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [
     * Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/WalletConstants.constant-summary)
     */
    private fun handleError(statusCode: Int) {
        Log.w(
            "loadPaymentData failed",
            String.format("Error code: %d", statusCode)
        )
    }

}