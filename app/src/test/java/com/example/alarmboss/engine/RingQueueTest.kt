package com.example.alarmboss.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RingQueueTest {

    private val first = RingRequest(1, isWakeCheck = false)
    private val second = RingRequest(2, isWakeCheck = false)
    private val third = RingRequest(3, isWakeCheck = true)

    @Test
    fun `the first ring becomes current straight away`() {
        val queue = RingQueue()
        assertTrue(queue.offer(first))
        assertEquals(first, queue.current)
        assertFalse(queue.hasPending)
    }

    @Test
    fun `a second ring waits instead of replacing the first`() {
        val queue = RingQueue()
        queue.offer(first)
        assertFalse(queue.offer(second))
        assertEquals(first, queue.current)
        assertTrue(queue.hasPending)
    }

    @Test
    fun `rings are shown in the order they rang`() {
        val queue = RingQueue()
        queue.offer(first)
        queue.offer(second)
        queue.offer(third)
        assertEquals(second, queue.advance())
        assertEquals(third, queue.advance())
        assertNull(queue.advance())
        assertNull(queue.current)
        assertFalse(queue.hasPending)
    }

    @Test
    fun `the same ring offered twice is ignored`() {
        val queue = RingQueue()
        queue.offer(first)
        assertFalse(queue.offer(first))
        queue.offer(second)
        assertFalse(queue.offer(second))
        assertEquals(second, queue.advance())
        assertNull(queue.advance())
    }

    @Test
    fun `an alarm and its wake-up check are different rings`() {
        val queue = RingQueue()
        queue.offer(RingRequest(1, isWakeCheck = false))
        queue.offer(RingRequest(1, isWakeCheck = true))
        assertTrue(queue.hasPending)
        assertEquals(RingRequest(1, true), queue.advance())
    }

    @Test
    fun `after the queue empties a new ring becomes current again`() {
        val queue = RingQueue()
        queue.offer(first)
        assertNull(queue.advance())
        assertTrue(queue.offer(second))
        assertEquals(second, queue.current)
    }

    @Test
    fun `advancing an empty queue is harmless`() {
        val queue = RingQueue()
        assertNull(queue.advance())
        assertNull(queue.current)
    }
}
