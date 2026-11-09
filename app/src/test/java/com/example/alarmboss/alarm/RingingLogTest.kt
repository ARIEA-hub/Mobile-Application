package com.example.alarmboss.alarm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RingingLogTest {

    private val now = 10_000_000L

    @Test
    fun `an entry survives being stored and read back`() {
        val primary = RingingLog.Entry(7, null, 123)
        val check = RingingLog.Entry(7, "WAKE_CHECK", 456)
        val decoded = RingingLog.decode(setOf(RingingLog.encode(primary), RingingLog.encode(check)))
        assertEquals(setOf(primary, check), decoded.toSet())
    }

    @Test
    fun `malformed entries are dropped instead of crashing`() {
        val good = RingingLog.encode(RingingLog.Entry(1, null, 5))
        val decoded = RingingLog.decode(setOf(good, "", "garbage", "x|y|z", "1|2", "1||notatime", "1|a|2|3"))
        assertEquals(listOf(RingingLog.Entry(1, null, 5)), decoded)
    }

    @Test
    fun `nothing stored decodes to nothing`() {
        assertTrue(RingingLog.decode(null).isEmpty())
    }

    @Test
    fun `a recent ring is resumed`() {
        val entries = listOf(RingingLog.Entry(1, null, now - 60_000))
        assertEquals(entries, RingingLog.fresh(entries, now))
    }

    @Test
    fun `an old ring is treated as abandoned`() {
        val old = RingingLog.Entry(1, null, now - RingingLog.MAX_AGE_MS - 1)
        assertTrue(RingingLog.fresh(listOf(old), now).isEmpty())
    }

    @Test
    fun `a ring right at the age limit is still resumed`() {
        val edge = RingingLog.Entry(1, null, now - RingingLog.MAX_AGE_MS)
        assertEquals(listOf(edge), RingingLog.fresh(listOf(edge), now))
    }

    @Test
    fun `an entry stamped in the future is ignored`() {
        // A clock that was set back after the ring started: resuming it could ring at the wrong time.
        assertTrue(RingingLog.fresh(listOf(RingingLog.Entry(1, null, now + 1)), now).isEmpty())
    }

    @Test
    fun `resumed rings come back oldest first`() {
        val later = RingingLog.Entry(2, null, now - 1_000)
        val earlier = RingingLog.Entry(1, null, now - 5_000)
        assertEquals(listOf(earlier, later), RingingLog.fresh(listOf(later, earlier), now))
    }

    @Test
    fun `adding a ring that is already recorded replaces it`() {
        val entries = RingingLog.plus(listOf(RingingLog.Entry(1, null, 100)), RingingLog.Entry(1, null, 200))
        assertEquals(listOf(RingingLog.Entry(1, null, 200)), entries)
    }

    @Test
    fun `an alarm and its wake-up check are recorded separately`() {
        val entries = RingingLog.plus(listOf(RingingLog.Entry(1, null, 100)), RingingLog.Entry(1, "WAKE_CHECK", 200))
        assertEquals(2, entries.size)
    }

    @Test
    fun `removing one ring leaves the others`() {
        val entries = listOf(RingingLog.Entry(1, null, 1), RingingLog.Entry(2, null, 2))
        assertEquals(listOf(RingingLog.Entry(2, null, 2)), RingingLog.minus(entries, 1, null))
    }
}
