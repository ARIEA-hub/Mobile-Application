package com.example.alarmboss.counselor

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiClientTest {

    private fun counselor(text: String) = ChatMessage(sender = MessageSender.COUNSELOR, text = text)
    private fun user(text: String) = ChatMessage(sender = MessageSender.USER, text = text)

    // ---- the conversation sent to the API must open with the user and alternate ----

    @Test
    fun `the counselor greeting at the top is dropped so the request opens with the user`() {
        val turns = GeminiTurns.build(listOf(counselor("hey there"), user("hi")), "hi")
        assertEquals(listOf(Turn(Turn.USER, "hi")), turns)
    }

    @Test
    fun `the message just typed is not sent twice`() {
        val history = listOf(counselor("hello"), user("first"), counselor("answer"), user("second"))
        val turns = GeminiTurns.build(history, "second")
        assertEquals(listOf(Turn.USER, Turn.MODEL, Turn.USER), turns.map { it.role })
        assertEquals("second", turns.last().text)
    }

    @Test
    fun `consecutive messages from one side are merged`() {
        // Two questions in a row happen whenever an earlier reply failed to arrive.
        val history = listOf(user("one"), user("two"), user("three"))
        val turns = GeminiTurns.build(history, "three")
        assertEquals(1, turns.size)
        assertEquals("one\ntwo\nthree", turns[0].text)
    }

    @Test
    fun `a window that starts mid-conversation on a counselor reply still opens with the user`() {
        val history = (1..10).flatMap { listOf(user("q$it"), counselor("a$it")) } + user("latest")
        val turns = GeminiTurns.build(history, "latest")
        assertEquals(Turn.USER, turns.first().role)
        assertEquals("latest", turns.last().text)
        turns.zipWithNext().forEach { (a, b) -> assertTrue("roles must alternate", a.role != b.role) }
    }

    @Test
    fun `only the most recent turns are kept`() {
        val history = (1..30).map { if (it % 2 == 1) user("u$it") else counselor("c$it") }
        assertTrue(GeminiTurns.build(history, "x").size <= 8)
    }

    @Test
    fun `blank messages are ignored and the new message is always last`() {
        val turns = GeminiTurns.build(listOf(user("  "), counselor(""), user("real")), "new")
        // Blank turns vanish; the real earlier message and the new one are consecutive, so they merge.
        assertEquals(listOf(Turn(Turn.USER, "real" + "\n" + "new")), turns)
    }

    // ---- request and response shapes ----

    @Test
    fun `the request carries the system prompt, the turns and the limits`() {
        val body = JSONObject(
            GeminiClient.requestBody("be kind", listOf(Turn(Turn.USER, "hi")), maxTokens = 100, temperature = 0.5)
        )
        assertEquals("be kind", body.getJSONObject("system_instruction").getJSONArray("parts").getJSONObject(0).getString("text"))
        val content = body.getJSONArray("contents").getJSONObject(0)
        assertEquals("user", content.getString("role"))
        assertEquals("hi", content.getJSONArray("parts").getJSONObject(0).getString("text"))
        assertEquals(100, body.getJSONObject("generationConfig").getInt("maxOutputTokens"))
    }

    @Test
    fun `a request without a system prompt leaves it out`() {
        val body = JSONObject(GeminiClient.requestBody(null, listOf(Turn(Turn.USER, "hi")), 50, 0.7))
        assertTrue(!body.has("system_instruction"))
    }

    @Test
    fun `the reply text is read from the first candidate`() {
        val json = """{"candidates":[{"content":{"parts":[{"text":"  hello "},{"text":"there"}]}}]}"""
        assertEquals("hello there", GeminiClient.parseReply(json))
    }

    @Test
    fun `a blocked, empty or malformed response yields no text instead of crashing`() {
        assertNull(GeminiClient.parseReply("""{"promptFeedback":{"blockReason":"SAFETY"}}"""))
        assertNull(GeminiClient.parseReply("""{"candidates":[]}"""))
        assertNull(GeminiClient.parseReply("""{"candidates":[{"content":{"parts":[{"text":"   "}]}}]}"""))
        assertNull(GeminiClient.parseReply("not json at all"))
        assertNull(GeminiClient.parseReply(""))
    }

    @Test
    fun `a conversation that does not start with the user is refused up front`() {
        try {
            GeminiClient("key").generate(null, listOf(Turn(Turn.MODEL, "hi")), 10)
            throw AssertionError("expected an IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // good: a programming error caught before any network use
        }
    }

    @Test
    fun `no time left means no attempt is made`() {
        try {
            GeminiClient("key").generate(null, listOf(Turn(Turn.USER, "hi")), 10, budgetMs = 0)
            throw AssertionError("expected a GeminiException")
        } catch (e: GeminiException) {
            assertEquals(0, e.code)
        }
    }
}
