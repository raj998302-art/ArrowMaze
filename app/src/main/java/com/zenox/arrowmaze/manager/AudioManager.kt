package com.zenox.arrowmaze.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.ToneGenerator
import android.util.Log

class AudioManager constructor(
    private val context: Context
) {

    private var soundPool: SoundPool? = null
    private var mediaPlayer: MediaPlayer? = null
    private var musicVolume = 1f
    private var sfxVolume = 1f
    private var isMusicEnabled = true
    private var isSfxEnabled = true
    private var toneGenerator: ToneGenerator? = null
    private var musicToneThread: Thread? = null
    @Volatile
    private var isMusicPlaying = false

    enum class Sound {
        TAP, BUTTON, COIN, BUY, HINT, WRONG, UNLOCK, VICTORY
    }

    private val soundIds = mutableMapOf<Sound, Int>()

    fun init() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()

        toneGenerator = ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 100)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setVolume(0f, 0f)
        }

        Log.d(TAG, "AudioManager initialized")
    }

    fun playSound(sound: Sound) {
        if (!isSfxEnabled || toneGenerator == null) return

        val tg = toneGenerator ?: return

        try {
            val (toneType, durationMs) = when (sound) {
                Sound.TAP -> ToneGenerator.TONE_PROP_BEEP to 50
                Sound.BUTTON -> ToneGenerator.TONE_PROP_BEEP2 to 60
                Sound.COIN -> ToneGenerator.TONE_CDMA_ABBR_ALERT to 120
                Sound.BUY -> ToneGenerator.TONE_CDMA_PIP to 100
                Sound.HINT -> ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE to 200
                Sound.WRONG -> ToneGenerator.TONE_CDMA_ABBR_REORDER to 300
                Sound.UNLOCK -> ToneGenerator.TONE_CDMA_ABBR_ALERT to 250
                Sound.VICTORY -> ToneGenerator.TONE_CDMA_PIP to 400
            }
            tg.startTone(toneType, durationMs)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to play sound: $sound", e)
        }
    }

    fun startMusic() {
        if (!isMusicEnabled || isMusicPlaying) return
        isMusicPlaying = true

        musicToneThread = Thread {
            val notes = intArrayOf(
                ToneGenerator.TONE_CDMA_DIAL_TONE_LITE,
                ToneGenerator.TONE_CDMA_ALERT_INCALL_LITE,
                ToneGenerator.TONE_CDMA_PIP,
                ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE,
                ToneGenerator.TONE_CDMA_ABBR_ALERT
            )
            val durations = longArrayOf(800, 800, 600, 600, 1000)

            while (isMusicPlaying) {
                val tg = toneGenerator ?: break

                try {
                    for (i in notes.indices) {
                        if (!isMusicPlaying) break
                        tg.startTone(notes[i], durations[i].toInt())
                        Thread.sleep(durations[i] + 200)
                    }
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    if (e is InterruptedException) break
                    Log.w(TAG, "Music playback error", e)
                    break
                }
            }
        }.apply {
            name = "AmbientMusicThread"
            isDaemon = true
            start()
        }

        Log.d(TAG, "Background music started")
    }

    fun stopMusic() {
        isMusicPlaying = false
        musicToneThread?.interrupt()
        musicToneThread = null
        toneGenerator?.stopTone()
        Log.d(TAG, "Background music stopped")
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            stopMusic()
        }
    }

    fun setSfxEnabled(enabled: Boolean) {
        isSfxEnabled = enabled
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
    }

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    fun release() {
        stopMusic()
        toneGenerator?.release()
        toneGenerator = null
        soundPool?.release()
        soundPool = null
        mediaPlayer?.release()
        mediaPlayer = null
        Log.d(TAG, "AudioManager released")
    }

    companion object {
        private const val TAG = "AudioManager"
    }
}
