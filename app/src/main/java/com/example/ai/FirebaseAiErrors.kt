package com.example.ai

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Failure categories for Firebase AI Logic calls. Used for user-facing messages. */
enum class AiFailureKind {
    NOT_CONFIGURED,
    APP_CHECK,
    QUOTA,
    NETWORK,
    EMPTY_RESPONSE,
    UNKNOWN,
}

/**
 * Maps a Firebase AI Logic failure to a failure kind and a user-facing message.
 *
 * Pure logic (no Firebase calls) so it can be unit-tested without a project.
 * The Firebase AI Logic SDK surfaces quota / permission failures as exceptions
 * whose messages contain the underlying status (e.g. 429 RESOURCE_EXHAUSTED,
 * 403 PERMISSION_DENIED, App Check rejections), so classification is
 * message-based and checks the whole cause chain.
 */
object FirebaseAiErrors {

    fun classify(error: Throwable): AiFailureKind {
        if (error is FirebaseAiNotConfiguredException) return AiFailureKind.NOT_CONFIGURED
        val message = buildString {
            var current: Throwable? = error
            while (current != null) {
                append(current.message.orEmpty())
                append(' ')
                current = current.cause
            }
        }.lowercase()
        return when {
            message.contains("not configured") ||
                message.contains("default firebaseapp") ||
                message.contains("firebaseapp is not initialized") ||
                message.contains("google-services.json") -> AiFailureKind.NOT_CONFIGURED

            message.contains("429") ||
                message.contains("resource_exhausted") ||
                message.contains("resource exhausted") ||
                message.contains("quota") ||
                message.contains("rate limit") ||
                message.contains("rate-limit") -> AiFailureKind.QUOTA

            message.contains("403") ||
                message.contains("permission_denied") ||
                message.contains("permission denied") ||
                message.contains("app check") ||
                message.contains("appcheck") ||
                message.contains("api key not valid") ||
                message.contains("api_key_not_valid") -> AiFailureKind.APP_CHECK

            message.contains("empty response") -> AiFailureKind.EMPTY_RESPONSE

            error is UnknownHostException ||
                error is SocketTimeoutException ||
                error is ConnectException ||
                error is IOException -> AiFailureKind.NETWORK

            message.contains("unable to resolve host") ||
                message.contains("network") ||
                message.contains("timeout") ||
                message.contains("timed out") -> AiFailureKind.NETWORK

            else -> AiFailureKind.UNKNOWN
        }
    }

    /** User-facing message for a failure. Never contains raw exception detail. */
    fun userMessage(error: Throwable): String = when (classify(error)) {
        AiFailureKind.NOT_CONFIGURED ->
            "AI isn't set up yet — this build has no Firebase configuration. " +
                "You can still enter the expense manually."

        AiFailureKind.APP_CHECK ->
            "AI was blocked by app verification (App Check). Please try again; " +
                "if it keeps happening, the Firebase App Check setup needs attention."

        AiFailureKind.QUOTA ->
            "AI is temporarily busy (usage limit reached). Please try again later " +
                "or enter the items manually."

        AiFailureKind.NETWORK ->
            "No connection to the AI service. Check your internet and try again, " +
                "or enter the items manually."

        AiFailureKind.EMPTY_RESPONSE ->
            "AI returned an empty result. Please try again or enter the items manually."

        AiFailureKind.UNKNOWN ->
            "AI couldn't process this right now. Please try again or enter the items manually."
    }

    fun toException(error: Throwable): Exception = Exception(userMessage(error), error)
}
