package br.com.tiker.ui.stickerPackage

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import br.com.tiker.BuildConfig
import br.com.tiker.R
import br.com.tiker.ui.adapter.StickerDefaultRecyclerAdapter
import br.com.tiker.ui.base.AddStickerPackActivity
import br.com.tiker.ui.base.AddStickerPackActivity.StickerPackNotAddedMessageFragment
import br.com.tiker.ui.stickerPackage.StickerPackageViewModel.Companion.ADD_PACK
import br.com.tiker.ui.stickerPackage.StickerPackageViewModel.Companion.EXTRA_STICKER_PACK_AUTHORITY
import br.com.tiker.ui.stickerPackage.StickerPackageViewModel.Companion.EXTRA_STICKER_PACK_ID
import br.com.tiker.ui.stickerPackage.StickerPackageViewModel.Companion.EXTRA_STICKER_PACK_NAME
import br.com.tiker.utils.alert
import br.com.tiker.utils.gone
import br.com.tiker.utils.observe
import br.com.tiker.utils.visible
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import io.cubos.r2d2lib.CubosActivity
import kotlinx.android.synthetic.main.activity_sticker_package.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class StickerPackageActivity : CubosActivity() {

    private val viewModel: StickerPackageViewModel by viewModel()
    private lateinit var adapter: StickerDefaultRecyclerAdapter
    private var stickerPackageId: Int = 0

    private var adCompleted = false
    private val rewardedVideoAd by lazy { MobileAds.getRewardedVideoAdInstance(this) }

    companion object {
        const val STICKER_PACKAGE_ID = "sticker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sticker_package)
        stickerPackageId = intent.getIntExtra(STICKER_PACKAGE_ID, 0)

        initUi()
        initListeners()
    }

    private fun initUi() {
        adapter = StickerDefaultRecyclerAdapter()

        recyclerView?.layoutManager = GridLayoutManager(this, 4)
        recyclerView?.adapter = adapter

        observe(viewModel.closeActivity) { closeActivity -> if (closeActivity) onBackPressed() }

        observe(viewModel.namePackage) { packageNameTXT?.text = it }
        observe(viewModel.authorPackage) { packageAuthorTXT?.text = it }
        observe(viewModel.stickerList) {
            adapter.updateList(it)
            adapter.notifyDataSetChanged()
        }

       observe(viewModel.uiEventLiveData) {
            when (it?.first) {
                1 -> {
                    try {
                        startActivityForResult(
                            Intent.createChooser(
                                it.second as Intent,
                                getString(R.string.add_to_whatsapp)
                            ), ADD_PACK
                        )
                    } catch (e: ActivityNotFoundException) {
//            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG)
//                .show()
                    }
                }
                2 -> {
                    try {
                        startActivityForResult(it.second as Intent, ADD_PACK)
                    } catch (e: ActivityNotFoundException) {
//            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG)
//                .show()
                    }
                }
            }
        }

        viewModel.loadSavedStickerList(stickerPackageId)
    }

    private fun initListeners() {
        sharePackageBTN?.setOnClickListener {
            configureAd()
            object : Thread() {
                override fun run() {
                    viewModel.createPackageToWhatsApp(this@StickerPackageActivity)
                }
            }.start()
        }

        removePackageBTN?.setOnClickListener {
            viewModel.removeStickerPackage(stickerPackageId)
        }

        backBTN?.setOnClickListener {
            onBackPressed()
        }
    }

    private fun configureAd() {
        rewardedVideoAd.rewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onRewardedVideoAdLoaded() {
                progressBar?.gone()
                if (rewardedVideoAd.isLoaded)
                    rewardedVideoAd.show()
            }

            override fun onRewardedVideoCompleted() { adCompleted = true }

            override fun onRewardedVideoAdClosed() {
                if (!adCompleted) {
                    alert(getString(R.string.error), getString(R.string.error_watch_add), getString(R.string.watch), false) {
                        configureAd()
                    }
                } else {
//                    if (viewModel.stickerPackAdded == false) {
//                        alert(getString(R.string.error), viewModel.error ?: getString(R.string.error_default))
//                    } else {
                        addStickerPackToWhatsApp(viewModel.stickerPack.identifier, viewModel.stickerPack.name)
//                    }
                }
            }

            override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                progressBar?.gone()
//                if (viewModel.stickerPackAdded == false) {
//                    alert(getString(R.string.error), viewModel.error ?: getString(R.string.error_default))
//                } else {
                    addStickerPackToWhatsApp(viewModel.stickerPack.identifier, viewModel.stickerPack.name)
//                }
            }

            override fun onRewardedVideoStarted() {}

            override fun onRewardedVideoAdOpened() {}

            override fun onRewarded(p0: RewardItem?) {}

            override fun onRewardedVideoAdLeftApplication() {}
        }
        val ad = if (BuildConfig.DEBUG) getString(R.string.cod_ad_debug) else getString(R.string.cod_ad_release)
        rewardedVideoAd.loadAd(ad, AdRequest.Builder().build())
        progressBar?.visible()
    }

    fun addStickerPackToWhatsApp(identifier: String?, stickerPackName: String?) {
        val intent = Intent()
        intent.action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
        intent.putExtra(EXTRA_STICKER_PACK_ID, identifier)
        intent.putExtra(EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY)
        intent.putExtra(EXTRA_STICKER_PACK_NAME, stickerPackName)
        try {
            startActivityForResult(intent, AddStickerPackActivity.ADD_PACK)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AddStickerPackActivity.ADD_PACK) {
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data != null) {
                    val validationError = data.getStringExtra("validation_error")
                    if (validationError != null) {
                        Log.e("AddStickerPackActivity", "Validation failed:$validationError")
                    }
                } else {
                    StickerPackNotAddedMessageFragment().show(supportFragmentManager, "sticker_pack_not_added")
                }
            } else {
                alert(getString(R.string.sticker_added), getString(R.string.back_whatsapp_see_package), getString(R.string.back), true) {
                    val launchIntent = packageManager.getLaunchIntentForPackage("com.whatsapp")
                    startActivity(launchIntent)
                }
            }
        }
    }

}
