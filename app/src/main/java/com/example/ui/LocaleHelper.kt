package com.example.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun ProvideLocalizedContext(
    language: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val locale = if (language == "en") Locale.ENGLISH else Locale("bn", "BD")
    
    val configuration = Configuration(LocalConfiguration.current).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    
    val localizedContext = context.createConfigurationContext(configuration)
    
    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
        content = content
    )
}
