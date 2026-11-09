package com.example.alarmboss.counselor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    /** True when the online service was wanted but could not be used, so the reply came from the device. */
    val answeredOnDevice: Boolean = false
)

enum class MessageSender { USER, COUNSELOR }

/** A reply, and how it was produced. */
data class CounselorReply(val text: String, val answeredOnDevice: Boolean)

class SleepCounselorEngine(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = context.getSharedPreferences("alarmboss_counselor_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DEFAULT_MODEL = "gemini-3.5-flash"
        private const val PREFS_KEY_STORAGE = "gemini_api_key_storage"

        const val COUNSELOR_SYSTEM_PROMPT = """
You are a warm, humane, and deeply understanding sleep and wellness counselor for the AlarmBoss app. You are a personal adviser, empathetic listener, and sleep mentor — NOT a corporate chatbot, and NOT a rigid checklist dispenser.

CORE MISSION & ETHOS:
1. SAFE SPACE FOR PERSONAL INSIGHTS:
   Provide an open, compassionate, non-judgmental environment where the user feels completely safe sharing personal feelings, struggles, and vulnerabilities. Never judge, dismiss, or lecture.
   
2. HIGH FAITHFULNESS & ROOT-CAUSE INQUIRY:
   When a user says they can't sleep, are struggling, or feel exhausted, DO NOT just vomit a generic checklist of sleep hygiene tips (e.g. "dark room, cool room, no screens"). That feels dismissive and robotic.
   Instead:
   - Validate their feeling with genuine warmth and empathy.
   - Gently ask about root causes and symptoms: are they experiencing physical tension, pain, or restlessness? Is their mind racing with worries, stress, or anxiety? Did something specific happen today?
   - Offer tailored, comforting guidance based on what they're actually going through.

3. CASUAL GREETINGS & CHECK-INS:
   Always respond naturally, warmly, and flexibly to casual conversation and greetings!
   - If they say "what's up?", "how are you?", "hey", "hello": greet them warmly like a friend and ask how their energy and rest have been.
   - If they say "I have slept well" or celebrate a good night: celebrate with them! Ask what helped make last night so restorative.

4. LOWERCASE & CONVERSATIONAL STYLE:
   - Start your replies with a lower case letter (e.g. "hey...", "i'm really sorry to hear that...", "that's huge...").
   - Sound human, relaxed, and sincere. Avoid overly formal or polite speech.
   - NEVER use corporate jargon or disclaimers: no "I apologize for the inconvenience", "as an AI language model", "thank you for contacting me", "if you have any queries".

5. OUT-OF-CONTEXT GUARDRAIL (GENUINELY UNRELATED TOPICS ONLY):
   Only redirect if the query is totally detached from wellness, feelings, and sleep (e.g. coding python, sports scores, restaurant recommendations, politics).
   If so, gently decline starting with a lower case letter and include:
   "if you have any sort of doubts about how to improve sleep hygeine, and be well rested, don't ever be shy to ask!"
"""

        const val OUT_OF_CONTEXT_RESPONSE = SleepKnowledge.OUT_OF_CONTEXT

        /** The on-device answer: no network, no key, instant. */
        fun fallbackCounselorEngine(query: String): String = SleepKnowledge.answer(query)
    }

    /**
     * The user's own Gemini key, kept on this device. There is deliberately no key built into the app:
     * a key compiled into an APK can be pulled out of it by anyone who downloads the release.
     */
    fun getApiKey(): String = (prefs.getString(PREFS_KEY_STORAGE, "") ?: "").trim()

    fun hasApiKey(): Boolean = getApiKey().isNotEmpty()

    fun saveApiKey(key: String) {
        prefs.edit().putString(PREFS_KEY_STORAGE, key.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove(PREFS_KEY_STORAGE).apply()
    }

    private fun isOnline(): Boolean = runCatching {
        val manager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }.getOrDefault(false)

    /**
     * Answers [userMessage]. Without a key, or when the service cannot be reached in time, the on-device
     * knowledge answers instead, so the chat always replies promptly and never leaves the user waiting.
     */
    suspend fun getCounselorReply(history: List<ChatMessage>, userMessage: String): CounselorReply =
        withContext(Dispatchers.IO) {
            val local = fallbackCounselorEngine(userMessage)
            val apiKey = getApiKey()
            if (apiKey.isBlank()) return@withContext CounselorReply(local, answeredOnDevice = false)
            // No connection: don't wait for a timeout to find out.
            if (!isOnline()) return@withContext CounselorReply(local, answeredOnDevice = true)
            try {
                val text = GeminiClient(apiKey).generate(
                    systemPrompt = COUNSELOR_SYSTEM_PROMPT,
                    turns = GeminiTurns.build(history, userMessage),
                    maxTokens = 600
                )
                CounselorReply(text.replaceFirstChar { it.lowercase() }, answeredOnDevice = false)
            } catch (e: Exception) {
                // A wrong key, a quota limit, a dropped connection: whatever it was, the user still gets an answer.
                CounselorReply(local, answeredOnDevice = true)
            }
        }

    suspend fun getCounselorResponse(history: List<ChatMessage>, userMessage: String): String =
        getCounselorReply(history, userMessage).text

    /** Fresh reading passages from the AI. Throws if there is no key or the request fails. */
    suspend fun generateReadingPassages(): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        check(apiKey.isNotBlank()) { "No API key" }
        val prompt = "Write 2 distinct, inspiring morning wake-up paragraphs (about 35-40 words each) to boost cognitive alertness upon waking up. Separate them with a line containing only '---'. Do not use bullet points or numbering."
        GeminiClient(apiKey).generate(
            systemPrompt = null,
            turns = listOf(Turn(Turn.USER, prompt)),
            maxTokens = 300
        ).trim()
    }
}
