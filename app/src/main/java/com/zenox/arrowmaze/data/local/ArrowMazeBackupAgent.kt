package com.zenox.arrowmaze.data.local

import android.app.backup.BackupAgentHelper
import android.app.backup.FileBackupHelper
import android.app.backup.SharedPreferencesBackupHelper

class ArrowMazeBackupAgent : BackupAgentHelper() {

    companion object {
        private const val PREFS_BACKUP_KEY = "arrowmaze_prefs"
        private const val FILES_BACKUP_KEY = "arrowmaze_files"
        private const val PREFS_NAME = "com.zenox.arrowmaze_preferences"
    }

    override fun onCreate() {
        // Backup SharedPreferences
        addHelper(
            PREFS_BACKUP_KEY,
            SharedPreferencesBackupHelper(this, PREFS_NAME)
        )

        // Backup additional files (e.g., local database if needed)
        addHelper(
            FILES_BACKUP_KEY,
            FileBackupHelper(this)
        )
    }

    override fun onBackup(
        oldState: android.os.ParcelFileDescriptor?,
        data: android.app.backup.BackupDataOutput?,
        newState: android.os.ParcelFileDescriptor?
    ) {
        // Delegate to super for standard backup handling
        super.onBackup(oldState, data, newState)
    }

    override fun onRestore(
        data: android.app.backup.BackupDataInput?,
        appVersionCode: Int,
        newState: android.os.ParcelFileDescriptor?
    ) {
        // Delegate to super for standard restore handling
        super.onRestore(data, appVersionCode, newState)
    }
}