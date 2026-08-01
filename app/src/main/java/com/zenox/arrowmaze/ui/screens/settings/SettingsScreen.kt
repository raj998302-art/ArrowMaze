package com.zenox.arrowmaze.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    BackHandler { navController.popBackStack() }

    // About dialog
    if (state.showAbout) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAbout() },
            title = { Text("About ArrowMaze") },
            text = {
                Text(
                    "ArrowMaze v1.0.0\n\n" +
                    "A puzzle game where you clear snakes by tapping them in the right order. " +
                    "Each snake shows an arrow indicating its exit direction. " +
                    "Remove all snakes to complete the level!"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAbout() }) { Text("OK") }
            }
        )
    }

    // Privacy policy dialog
    if (state.showPrivacyPolicy) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPrivacyPolicy() },
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    "ArrowMaze respects your privacy.\n\n" +
                    "- We store your game progress locally and on Firebase for sync.\n" +
                    "- We do not share your data with third parties.\n" +
                    "- You can delete your account at any time from the Profile screen."
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissPrivacyPolicy() }) { Text("OK") }
            }
        )
    }

    // Reset data dialog
    if (state.showResetDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResetDataDialog() },
            title = { Text("Reset All Data?") },
            text = { Text("This will reset all progress, coins, and settings. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissResetDataDialog() }) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissResetDataDialog() }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // --- Audio ---
            Text("Audio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            SettingsToggle(
                title = "Music",
                subtitle = "Background music",
                icon = { Icon(Icons.Outlined.MusicNote, contentDescription = null) },
                checked = state.musicEnabled,
                onCheckedChange = { viewModel.updateMusicEnabled(it) }
            )
            SettingsSlider(
                title = "Music Volume",
                value = state.musicVolume,
                onValueChange = { viewModel.updateMusicVolume(it) }
            )
            SettingsToggle(
                title = "Sound Effects",
                subtitle = "Tap and UI sounds",
                icon = { Icon(Icons.Outlined.VolumeUp, contentDescription = null) },
                checked = state.sfxEnabled,
                onCheckedChange = { viewModel.updateSfxEnabled(it) }
            )
            SettingsSlider(
                title = "SFX Volume",
                value = state.sfxVolume,
                onValueChange = { viewModel.updateSfxVolume(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Gameplay ---
            Text("Gameplay", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            SettingsToggle(
                title = "Vibration",
                subtitle = "Haptic feedback on taps",
                icon = { Icon(Icons.Outlined.Vibration, contentDescription = null) },
                checked = state.vibrationEnabled,
                onCheckedChange = { viewModel.updateVibrationEnabled(it) }
            )
            SettingsToggle(
                title = "Color Blind Mode",
                subtitle = "Use patterns for colorblind players",
                icon = { Icon(Icons.Outlined.Visibility, contentDescription = null) },
                checked = state.colorBlindMode,
                onCheckedChange = { viewModel.updateColorBlindMode(it) }
            )
            SettingsToggle(
                title = "Large Text",
                subtitle = "Increase text size",
                icon = { Icon(Icons.Outlined.FormatSize, contentDescription = null) },
                checked = state.largeText,
                onCheckedChange = { viewModel.updateLargeText(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Appearance ---
            Text("Appearance", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            SettingsToggle(
                title = "Dark Mode",
                subtitle = "Use dark theme",
                icon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                checked = state.darkMode,
                onCheckedChange = { viewModel.updateDarkMode(it) }
            )
            SettingsToggle(
                title = "Notifications",
                subtitle = "Daily challenge reminders",
                icon = { Icon(Icons.Outlined.Notifications, contentDescription = null) },
                checked = state.notificationsEnabled,
                onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Info ---
            Text("Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            SettingsRow(
                title = "About",
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                onClick = { viewModel.showAbout() }
            )
            SettingsRow(
                title = "Privacy Policy",
                icon = { Icon(Icons.Outlined.PrivacyTip, contentDescription = null) },
                onClick = { viewModel.showPrivacyPolicy() }
            )
            SettingsRow(
                title = "Reset All Data",
                icon = { Icon(Icons.Outlined.DeleteForever, contentDescription = null) },
                onClick = { viewModel.showResetDataDialog() },
                isDanger = true
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String = "",
    icon: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
 TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            title,
            color = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}