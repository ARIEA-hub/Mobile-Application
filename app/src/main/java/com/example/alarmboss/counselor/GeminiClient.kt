package com.example.alarmboss.counselor

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** One turn of a conversation as the Gemini API wants it: who spoke, and what they said. */
data class Turn(val role: String, val text: String) {
    companion object {
        const val USER = "user"
        const val MODEL = "model"
    }
}

/** A refusal or failure reported by the Gemini service. [code] is the HTTP status, or 0 for an empty answer. */
class GeminiException(val code: Int, message: String) : Exception(message)

/**
 * Turns the chat history into a request the API accepts. It insists that a conversation starts with
 * the user and strictly alternates; anything else is rejected with a 400, and the app's own greeting
 * (spoken by the counselor) sits at the top of every chat, so a naive history is invalid as soon as
 * the first messages scroll out of the window.
 */
object GeminiTurns {
    private const val MAX_HISTORY = 6

    fun build(history: List<ChatMessage>, userMessage: String): List<Turn> {
        // The screen adds the user's message to the history before asking, so it would appear twice.
        val past = history.toMutableList()
        val last = past.lastOrNull()
        if (last != null && last.sender == MessageSender.USER && last.text.trim() == userMessage.trim()) {
            past.removeAt(past.lastIndex)
        }

        val turns = ArrayList<Turn>()
        fun add(role: String, text: String) {
            val clean = text.trim()
            if (clean.isEmpty()) return
            val previous = turns.lastOrNull()
            if (previous != null && previous.role == role) {
                turns[turns.lastIndex] = previous.copy(text = previous.text + "\n" + clean)
            } else {
                turns += Turn(role, clean)
            }
        }
        past.takeLast(MAX_HISTORY).forEach { add(if (it.sender == MessageSender.USER) Turn.USER else Turn.MODEL, it.text) }
        // A conversation must open with the user.
        while (turns.isNotEmpty() && turns.first().role != Turn.USER) turns.removeAt(0)
        add(Turn.USER, userMessage)
        return turns
    }
}

/**
 * A small client for the Gemini `generateContent` endpoint, built to fail fast. The old code tried
 * several models in turn with fifteen-second timeouts each, so a bad key, a quota error or no
 * connection looked like the chat hanging for a minute or more. Now an unknown model (404) is the
 * only reason to try another one; every other failure is reported at once so the caller can fall back.
 */
class GeminiClient(private val apiKey: String) {

    fun generate(
        systemPrompt: String?,
        turns: List<Turn>,
        maxTokens: Int,
        temperature: Double = 0.7,
        budgetMs: Long = BUDGET_MS
    ): String {
        require(turns.isNotEmpty() && turns.first().role == Turn.USER) { "A conversation must start with the user" }
        val body = requestBody(systemPrompt, turns, maxTokens, temperature)
        // A blocking socket cannot be interrupted by a coroutine timeout, so the overall limit is enforced
        // here: every attempt gets only the time that is left.
        val deadline = System.currentTimeMillis() + budgetMs
        var unknownModel: GeminiException? = null
        for (model in MODELS) {
            val remaining = deadline - System.currentTimeMillis()
            if (remaining < MIN_ATTEMPT_MS) throw GeminiException(0, "Out of time")
            try {
                return post(model, body, minOf(CONNECT_TIMEOUT_MS.toLong(), remaining).toInt(), minOf(READ_TIMEOUT_MS.toLong(), remaining).toInt())
            } catch (e: GeminiException) {
                if (e.code != 404) throw e
                unknownModel = e
            }
        }
        throw unknownModel ?: GeminiException(0, "No model answered")
    }

    private fun post(model: String, body: String, connectMs: Int, readMs: Int): String {
        val connection = URL("$BASE_URL/$model:generateContent").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            // In a header rather than the address, which proxies and logs record.
            connection.setRequestProperty("x-goog-api-key", apiKey)
            connection.connectTimeout = connectMs
            connection.readTimeout = readMs
            connection.doOutput = true
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                val detail = runCatching {
                    BufferedReader(InputStreamReader(connection.errorStream ?: connection.inputStream)).use { it.readText() }
                }.getOrDefault("")
                throw GeminiException(code, "Gemini error $code: ${detail.take(200)}")
            }
            val text = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
            return parseReply(text) ?: throw GeminiException(0, "Gemini sent no text")
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        const val CONNECT_TIMEOUT_MS = 6_000
        const val READ_TIMEOUT_MS = 15_000

        /** The most the whole request may take, across every attempt. */
        const val BUDGET_MS = 20_000L
        private const val MIN_ATTEMPT_MS = 1_000L

        /** The preferred model, then the previous generation in case the first name is not available. */
        val MODELS = listOf(SleepCounselorEngine.DEFAULT_MODEL, "gemini-2.5-flash").distinct()

        fun requestBody(systemPrompt: String?, turns: List<Turn>, maxTokens: Int, temperature: Double): String =
            JSONObject().apply {
                if (systemPrompt != null) {
                    put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
                }
                put("contents", JSONArray().apply {
                    turns.forEach { turn ->
                        put(JSONObject().put("role", turn.role).put("parts", JSONArray().put(JSONObject().put("text", turn.text))))
                    }
                })
                put("generationConfig", JSONObject().put("temperature", temperature).put("maxOutputTokens", maxTokens))
            }.toString()

        /** The reply text from a response body, or null if it holds none (blocked, or an empty answer). */
        fun parseReply(responseBody: String): String? = runCatching {
            val candidate = JSONObject(responseBody).optJSONArray("candidates")?.optJSONObject(0) ?: return@runCatching null
            val parts = candidate.optJSONObject("content")?.optJSONArray("parts") ?: return@runCatching null
            buildString {
                for (i in 0 until parts.length()) append(parts.optJSONObject(i)?.optString("text").orEmpty())
            }.trim().ifEmpty { null }
        }.getOrNull()
    }
}
