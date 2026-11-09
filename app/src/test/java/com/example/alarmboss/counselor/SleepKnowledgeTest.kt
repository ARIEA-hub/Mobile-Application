package com.example.alarmboss.counselor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepKnowledgeTest {

    private fun answer(q: String) = SleepKnowledge.answer(q)

    private fun assertAnswers(question: String, vararg expectedFragments: String) {
        val reply = answer(question)
        assertNotEquals("'$question' was refused: $reply", SleepKnowledge.OUT_OF_CONTEXT, reply)
        for (fragment in expectedFragments) {
            assertTrue("'$question' should mention '$fragment', got: $reply", reply.contains(fragment, ignoreCase = true))
        }
    }

    // ---- domain questions each get a real, specific answer ----

    @Test
    fun `sleep substance questions`() {
        assertAnswers("How does caffeine affect my deep sleep?", "half-life")
        assertAnswers("is coffee after lunch bad for me", "caffeine")
        assertAnswers("Does wine help me sleep?", "alcohol", "deep")
        assertAnswers("what about melatonin", "doctor or pharmacist")
        assertAnswers("are sleeping pills safe", "doctor or pharmacist")
    }

    @Test
    fun `habit and environment questions`() {
        assertAnswers("Why do screens keep me awake at night?", "blue light")
        assertAnswers("is scrolling in bed bad", "blue light")
        assertAnswers("What temperature is best for my room?", "65")
        assertAnswers("my room is too hot at night", "temperature")
        assertAnswers("how do I block out noise", "white noise")
        assertAnswers("should I exercise in the evening", "2 to 3 hours")
        assertAnswers("when should I eat dinner", "two to three hours")
        assertAnswers("what mattress or pillow is best", "pillow")
    }

    @Test
    fun `schedule duration and recovery questions`() {
        assertAnswers("How do I fix my irregular sleep schedule?", "wake")
        assertAnswers("how many hours of sleep do I need", "7 to 9")
        assertAnswers("can I catch up on sleep at the weekend", "lie-in")
        assertAnswers("tips for jet lag", "bright light")
        assertAnswers("I work night shifts", "night shifts")
        assertAnswers("How long should I power nap?", "20 to 25")
    }

    @Test
    fun `night waking and early waking`() {
        assertAnswers("I keep waking up at 3am", "20 minutes")
        assertAnswers("I wake up too early and can't get back to sleep", "early")
    }

    @Test
    fun `mornings and waking up`() {
        assertAnswers("What should I do about morning grogginess?", "sleep inertia")
        assertAnswers("I always hit snooze", "snoozing")
        assertAnswers("I'm a heavy sleeper and miss my alarm", "challenge alarm")
    }

    @Test
    fun `health topics point to a doctor where they should`() {
        assertAnswers("my partner says I snore loudly", "doctor")
        assertAnswers("I wake up gasping for air", "sleep apnea")
        assertAnswers("my legs twitch when I lie down", "doctor")
        assertAnswers("I keep having nightmares", "doctor or therapist")
    }

    @Test
    fun `worries and a racing mind are met with real help`() {
        assertAnswers("I'm feeling very anxious and stressed", "brain dump", "4-7-8")
        assertAnswers("my mind is racing at night", "brain dump")
        assertAnswers("my shoulders ache and I'm tense", "muscle relaxation")
        assertAnswers("how do I relax before bed", "wind-down")
        assertAnswers("can you teach me breathing exercises", "4-7-8")
    }

    @Test
    fun `can't sleep asks about the cause and mentions long term help when it is ongoing`() {
        val now = answer("I can't sleep at night.")
        assertTrue(now.contains("anxiety") && now.contains("restless"))
        assertFalse(now.contains("cbt-i"))
        val ongoing = answer("I can't sleep, it's been like this for weeks")
        assertTrue(ongoing.contains("cbt-i"))
    }

    @Test
    fun `kids and the app itself are covered`() {
        assertAnswers("how much sleep does my child need", "9 to 12")
        assertAnswers("what is strict mode", "easy", "medium", "strict")
        assertAnswers("how does the wake check work", "wake-up check")
        assertAnswers("how do I set an alarm", "+ button")
        assertAnswers("my alarm won't stop", "emergency stop")
    }

    // ---- bugs the old keyword ladder had ----

    @Test
    fun `a sleep question that mentions something else is still answered`() {
        assertAnswers("I can't sleep before my python exam", "barrier")
    }

    @Test
    fun `whole words only, so decode and bedroom do not trigger other rules`() {
        // "decode" used to look like "code", and "bedroom" like "bed"; neither is a topic word.
        assertEquals(SleepKnowledge.OUT_OF_CONTEXT, answer("can you decode this message"))
        assertEquals(SleepKnowledge.OUT_OF_CONTEXT, answer("my bedroom colour is blue"))
    }

    @Test
    fun `a good-sleep question is not mistaken for a report of good sleep`() {
        val reply = answer("how do I get good sleep")
        assertFalse(reply.contains("wonderful to hear"))
    }

    @Test
    fun `reporting good sleep is celebrated but not when it is negated`() {
        assertTrue(answer("I slept well last night").contains("wonderful to hear"))
        assertFalse(answer("I haven't slept well, not at all").contains("wonderful to hear"))
    }

    // ---- unrelated questions are declined, kindly ----

    @Test
    fun `unrelated questions get the gentle redirect`() {
        listOf(
            "what is the most popular restaurant",
            "where can I get pizza tonight?",
            "how do I write a binary search in python?",
            "who is the president of france?",
            "what is the weather like in tokyo?",
            "who won the football match?",
            "what's the weather in the morning",
            "tell me a joke about cats"
        ).forEach { assertEquals("'$it'", SleepKnowledge.OUT_OF_CONTEXT, answer(it)) }
    }

    @Test
    fun `empty or symbol-only messages never crash`() {
        assertEquals(SleepKnowledge.OUT_OF_CONTEXT, answer(""))
        assertEquals(SleepKnowledge.OUT_OF_CONTEXT, answer("   "))
        assertEquals(SleepKnowledge.OUT_OF_CONTEXT, answer("?!?! ..."))
    }

    @Test
    fun `curly apostrophes and capitals are understood`() {
        assertAnswers("I CAN’T SLEEP", "barrier")
        assertAnswers("WHAT’S UP", "hey there")
    }

    // ---- small talk ----

    @Test
    fun `greetings thanks and good night`() {
        assertTrue(answer("hello").startsWith("hey there"))
        assertTrue(answer("hey there").startsWith("hey there"))
        assertTrue(answer("thanks so much").startsWith("you're always welcome"))
        assertTrue(answer("thank you").startsWith("you're always welcome"))
        assertTrue(answer("good night").startsWith("good night"))
        assertTrue(answer("good morning").startsWith("morning"))
    }

    @Test
    fun `a long message that starts with hello is treated as a real question`() {
        assertAnswers("hello, I'd love to know how caffeine affects my sleep at night", "caffeine")
    }

    // ---- safety ----

    @Test
    fun `a message about self harm gets a caring reply that points to real help`() {
        listOf(
            "I want to die",
            "I'm thinking about suicide",
            "sometimes I want to hurt myself",
            "I can't sleep and I want to end my life"
        ).forEach {
            val reply = answer(it)
            assertTrue("'$it' -> $reply", reply.contains("emergency number or crisis line"))
            assertFalse(reply.contains("hygeine"))
        }
    }

    // ---- tone rules that apply to every reply ----

    @Test
    fun `every reply is lowercase-first, warm and free of corporate phrasing`() {
        val questions = listOf(
            "hello", "thanks", "good night", "I can't sleep", "caffeine", "screens", "nap", "temperature", "schedule",
            "alcohol", "exercise", "melatonin", "snoring", "nightmares", "anxious", "how many hours of sleep",
            "pizza", "I want to die", "what is strict mode", "jet lag", "dinner", "pillow", "noise", "wake up at 3am",
            "sleep", "my child", "restless legs", "breathing", "groggy", "snooze", "feeling down", "sunlight"
        )
        for (q in questions) {
            val reply = answer(q)
            assertTrue("'$q' -> $reply", reply.first().isLowerCase())
            assertFalse(reply.contains("i'm sorry", ignoreCase = true))
            assertFalse(reply.contains("if you have any queries", ignoreCase = true))
            assertFalse(reply.contains("as an ai language model", ignoreCase = true))
            assertFalse(reply.contains("feel free to reach out", ignoreCase = true))
        }
    }

    @Test
    fun `the redirect carries the required closing sentence`() {
        assertTrue(
            SleepKnowledge.OUT_OF_CONTEXT.contains(
                "if you have any sort of doubts about how to improve sleep hygeine, and be well rested, don't ever be shy to ask!"
            )
        )
    }
}
