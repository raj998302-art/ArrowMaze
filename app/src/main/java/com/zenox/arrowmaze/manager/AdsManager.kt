package com.zenox.arrowmaze.manager

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var adLoadCallback: ((Boolean) -> Unit)? = null

    fun initialize() {
        MobileAds.initialize(context) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for ((adapterClass, status) in statusMap) {
                Log.d(TAG, "Ad adapter: $adapterClass - ${status.description}")
            }
            Log.d(TAG, "MobileAds initialized")
        }
    }

    fun loadInterstitial() {
        val adUnitId = getTestInterstitialId()
        if (adUnitId.isEmpty()) {
            Log.w(TAG, "Interstitial ad unit ID is empty - skipping load")
            return
        }
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                Log.d(TAG, "Interstitial ad loaded")
                adLoadCallback?.invoke(true)
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitialAd = null
                Log.w(TAG, "Interstitial ad failed to load: ${error.message}")
                adLoadCallback?.invoke(false)
            }
        })
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            Log.w(TAG, "Interstitial ad not ready")
            onDismissed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                Log.d(TAG, "Interstitial ad dismissed")
                onDismissed()
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                Log.w(TAG, "Interstitial ad failed to show: ${error.message}")
                onDismissed()
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial ad showed")
            }
            override fun onAdClicked() {
                Log.d(TAG, "Interstitial ad clicked")
            }
            override fun onAdImpression() {
                Log.d(TAG, "Interstitial ad impression")
            }
        }
        ad.show(activity)
    }

    fun loadRewarded() {
        val adUnitId = getTestRewardedId()
        if (adUnitId.isEmpty()) {
            Log.w(TAG, "Rewarded ad unit ID is empty - skipping load")
            return
        }
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                Log.d(TAG, "Rewarded ad loaded")
                adLoadCallback?.invoke(true)
            }
            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAd = null
                Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                adLoadCallback?.invoke(false)
            }
        })
    }

    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdFailed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            Log.w(TAG, "Rewarded ad not ready")
            onAdFailed()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                Log.d(TAG, "Rewarded ad dismissed")
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                Log.w(TAG, "Rewarded ad failed to show: ${error.message}")
                onAdFailed()
            }
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed")
            }
        }
        ad.show(activity) { rewardItem: RewardItem ->
            Log.d(TAG, "User earned reward: type=${rewardItem.type}, amount=${rewardItem.amount}")
            onUserEarnedReward()
        }
    }

    fun isAdReady(): Boolean = interstitialAd != null || rewardedAd != null

    fun destroy() {
        interstitialAd = null
        rewardedAd = null
        adLoadCallback = null
        Log.d(TAG, "AdsManager destroyed")
    }

    companion object {
        private const val TAG = "AdsManager"
        fun getTestBannerId(): String =
            if (com.zenox.arrowmaze.BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111" else ""
        fun getTestInterstitialId(): String =
            if (com.zenox.arrowmaze.BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712" else ""
        fun getTestRewardedId(): String =
            if (com.zenox.arrowmaze.BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else ""
    }
}
