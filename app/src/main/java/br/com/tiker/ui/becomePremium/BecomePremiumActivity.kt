package br.com.tiker.ui.becomePremium

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.tiker.R
import br.com.tiker.persistence.FirebaseDB
import br.com.tiker.utils.alert
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_become_premium.*
import java.util.*


class BecomePremiumActivity : AppCompatActivity(), PurchasesUpdatedListener, SkuDetailsResponseListener {

    lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_become_premium)
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
        initListeners()
    }


    private fun initListeners() {
        gPayButton?.setOnClickListener {
            startBillingConnection()
        }
        noMoneyBTN?.setOnClickListener {
            alert(
                    getString(R.string.google_rewards),
                    getString(R.string.google_rewards_download),
                    getString(
                            R.string.download
                    ),
                    true
            ) {
                startActivity(
                        Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.google_rewards_app))
                        )
                )
            }
        }
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                showError()
            }
        })
    }

    fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("conta_premium_full")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(params.build(), this)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            FirebaseDB.setUserIsPremium(true)
            alert(
                    getString(R.string.success_payment),
                    getString(R.string.success_payment_description),
                    getString(
                            R.string.ok
                    ),
                    true
            ) {
                finish()
            }
        }
    }

    override fun onSkuDetailsResponse(result: BillingResult, purchaseList: MutableList<SkuDetails>?) {
        if (!purchaseList.isNullOrEmpty()) {
            val flowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(purchaseList.first())
                    .build()
            val billingResult = billingClient.launchBillingFlow(this, flowParams)
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) showError()
        } else {
            showError()
        }
    }

    private fun showError() {
        alert(
                getString(R.string.error),
                getString(R.string.error_default_2),
                getString(
                        R.string.ok
                ),
                true
        ) {
            finish()
        }
    }
}