package com.example.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistryOwner
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
    val locale = if (language == "en") Locale.ENGLISH else Locale.forLanguageTag("bn-BD")
    
    val configuration = Configuration(LocalConfiguration.current).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    
    val localizedContext = context.createConfigurationContext(configuration)
    val activityResultRegistryOwner = LocalActivityResultRegistryOwner.current
        ?: (context as? ActivityResultRegistryOwner)
        ?: findActivityResultRegistryOwner(context)
    
    if (activityResultRegistryOwner != null) {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides configuration,
            LocalActivityResultRegistryOwner provides activityResultRegistryOwner,
            content = content
        )
    } else {
        CompositionLocalProvider(
            LocalContext provides localizedContext,
            LocalConfiguration provides configuration,
            content = content
        )
    }
}

private tailrec fun findActivityResultRegistryOwner(context: Context?): ActivityResultRegistryOwner? {
    return when (context) {
        null -> null
        is ActivityResultRegistryOwner -> context
        is ContextWrapper -> findActivityResultRegistryOwner(context.baseContext)
        else -> null
    }
}
