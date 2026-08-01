package com.zenox.arrowmaze

import android.app.Application
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

@HiltAndroidApp
class ArrowMazeApp : Application() {

    private val TAG = "ArrowMaze"

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Disable Crashlytics in debug builds
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)

        // Initialize analytics
        Firebase.analytics

        // Configure Remote Config defaults
        configureRemoteConfig()

        // Ensure anonymous auth for first-time users
        ensureAnonymousAuth()
    }

    private fun configureRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        val defaults = mapOf(
            "daily_challenge_enabled" to true,
            "max_hearts" to 3,
            "heart_regen_minutes" to 20,
            "ads_enabled" to !BuildConfig.DEBUG,
            "min_app_version" to 1,
            "leaderboard_page_size" to 20,
            "event_banner_text" to "",
            "event_banner_enabled" to false,
            "rewarded_ad_coins" to 50,
            "rewarded_ad_hints" to 2
        )
        remoteConfig.setDefaultsAsync(defaults)
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Remote config fetched and activated")
            } else {
                Log.w(TAG, "Remote config fetch failed", task.exception)
            }
        }
    }

    private fun ensureAnonymousAuth() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInAnonymously().result
                    Log.d(TAG, "Anonymous auth successful")
                } catch (e: Exception) {
                    Log.e(TAG, "Anonymous auth failed", e)
                }
            }
        }
    }

    companion object {
        lateinit var instance: ArrowMazeApp
            private set
    }
}