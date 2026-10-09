package com.example.ai

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object ReceiptOcrScanner {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Extracts text from an image bitmap using Google Play Services ML Kit.
     * Runs 100% on-device, offline, with zero API key configuration.
     */
    suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (continuation.isActive) {
                        continuation.resume(visionText.text)
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume("")
                    }
                }
        } catch (e: Throwable) {
            if (continuation.isActive) {
                continuation.resume("")
            }
        }
    }

    /**
     * Opens Google Lens or native visual scanner if installed on device.
     */
    fun openGoogleLens(context: Context, imageUri: Uri? = null) {
        try {
            // First try launching Google Lens with image or camera view
            val lensIntent = Intent(Intent.ACTION_VIEW).apply {
                if (imageUri != null) {
                    setDataAndType(imageUri, "image/*")
                }
                setPackage("com.google.ar.lens")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (lensIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(lensIntent)
                return
            }

            // Fallback 1: Package launch intent
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.ar.lens")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return
            }

            // Fallback 2: Google App visual search
            val googleSearchIntent = Intent(Intent.ACTION_VIEW).apply {
                if (imageUri != null) {
                    setDataAndType(imageUri, "image/*")
                }
                setPackage("com.google.android.googlequicksearchbox")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (googleSearchIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(googleSearchIntent)
                return
            }

            // Fallback 3: Generic image chooser if imageUri present
            if (imageUri != null) {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(imageUri, "image/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                    "Scan with Lens / View Image"
                )
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                return
            }

            Toast.makeText(context, "Google Lens is not installed on this device", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Google Lens", Toast.LENGTH_SHORT).show()
        }
    }
}
