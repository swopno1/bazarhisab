package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.manual.ManualEntryScreen
import com.example.ui.receipt.ReceiptScanScreen
import com.example.ui.reconciliation.ReconciliationScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.shopping.ShoppingListScreen
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    HISTORY,
    SHOPPING,
    SETTINGS,
    SCAN,
    VOICE,
    MANUAL,
    RECONCILIATION
}

@Composable
fun MainAppScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val language = settings?.language ?: "bn"
    val isBengali = language == "bn"

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var previousScreen by remember { mutableStateOf(Screen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ProvideLocalizedContext(language = language) {
        val isBottomBarVisible = currentScreen in listOf(Screen.HOME, Screen.HISTORY, Screen.SHOPPING, Screen.SETTINGS)

        Scaffold(
            bottomBar = {
                if (isBottomBarVisible) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.HOME,
                            onClick = { currentScreen = Screen.HOME },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == Screen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = stringResource(R.string.nav_home)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_home),
                                    fontSize = 12.sp,
                                    fontWeight = if (currentScreen == Screen.HOME) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.HISTORY,
                            onClick = { currentScreen = Screen.HISTORY },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == Screen.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                                    contentDescription = stringResource(R.string.nav_history)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_history),
                                    fontSize = 12.sp,
                                    fontWeight = if (currentScreen == Screen.HISTORY) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_history")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.SHOPPING,
                            onClick = { currentScreen = Screen.SHOPPING },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == Screen.SHOPPING) Icons.Filled.Description else Icons.Outlined.Description,
                                    contentDescription = stringResource(R.string.nav_shopping)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_shopping),
                                    fontSize = 12.sp,
                                    fontWeight = if (currentScreen == Screen.SHOPPING) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_shopping")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.SETTINGS,
                            onClick = { currentScreen = Screen.SETTINGS },
                            icon = {
                                Icon(
                                    imageVector = if (currentScreen == Screen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = stringResource(R.string.nav_settings)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_settings),
                                    fontSize = 12.sp,
                                    fontWeight = if (currentScreen == Screen.SETTINGS) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            // Back handler for sub-screens
            if (!isBottomBarVisible) {
                BackHandler {
                    currentScreen = previousScreen
                }
            }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToScan = {
                            previousScreen = Screen.HOME
                            currentScreen = Screen.SCAN
                        },
                        onNavigateToVoice = {
                            previousScreen = Screen.HOME
                            currentScreen = Screen.VOICE
                        },
                        onNavigateToShopping = {
                            currentScreen = Screen.SHOPPING
                        },
                        onNavigateToManual = {
                            previousScreen = Screen.HOME
                            currentScreen = Screen.MANUAL
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    Screen.HISTORY -> HistoryScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )

                    Screen.SHOPPING -> ShoppingListScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME },
                        onNavigateToReconciliation = {
                            previousScreen = Screen.SHOPPING
                            currentScreen = Screen.RECONCILIATION
                        },
                        modifier = Modifier.padding(innerPadding)
                    )

                    Screen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )

                    Screen.SCAN -> ReceiptScanScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME },
                        onNavigateToReconciliation = {
                            previousScreen = Screen.SCAN
                            currentScreen = Screen.RECONCILIATION
                        }
                    )

                    Screen.VOICE -> com.example.ui.voice.VoiceEntryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME },
                        onNavigateToReconciliation = {
                            previousScreen = Screen.VOICE
                            currentScreen = Screen.RECONCILIATION
                        }
                    )

                    Screen.MANUAL -> ManualEntryScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.HOME },
                        onNavigateToReconciliation = {
                            previousScreen = Screen.MANUAL
                            currentScreen = Screen.RECONCILIATION
                        }
                    )

                    Screen.RECONCILIATION -> ReconciliationScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = previousScreen },
                        onExpenseSaved = {
                            currentScreen = Screen.HOME
                            val message = if (isBengali) "খরচ সফলভাবে সংরক্ষিত হয়েছে!" else "Expense saved successfully!"
                            scope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    )
                }
            }
        }
    }
}
