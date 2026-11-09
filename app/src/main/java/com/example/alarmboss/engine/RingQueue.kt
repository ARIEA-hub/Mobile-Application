package com.example.alarmboss.engine

/** One alarm (or its wake-up check) waiting for the user's attention on the ringing screen. */
data class RingRequest(val alarmId: Long, val isWakeCheck: Boolean)

/**
 * The alarms that need the ringing screen, in the order they rang. There is only one ringing screen,
 * so when a second alarm starts while the first is still on screen it must wait its turn rather than
 * be dropped: otherwise a Strict alarm could be skipped just by solving an easier alarm that happened
 * to be ringing at the same time.
 */
class RingQueue {
    private val waiting = ArrayDeque<RingRequest>()

    /** The ring the user is dealing with now, or null when nothing is ringing. */
    var current: RingRequest? = null
        private set

    val hasPending: Boolean get() = waiting.isNotEmpty()

    /**
     * Adds a ring. It becomes [current] if nothing is showing, otherwise it waits. A request already
     * showing or already waiting is ignored, so the same intent delivered twice (a re-created screen,
     * a repeated notification tap) never produces a duplicate turn. Returns true if it is now current.
     */
    fun offer(request: RingRequest): Boolean {
        if (request == current || request in waiting) return false
        if (current == null) {
            current = request
            return true
        }
        waiting.addLast(request)
        return false
    }

    /** Finishes the current ring and moves on. Returns the next ring, or null if none is waiting. */
    fun advance(): RingRequest? {
        current = waiting.removeFirstOrNull()
        return current
    }
}
