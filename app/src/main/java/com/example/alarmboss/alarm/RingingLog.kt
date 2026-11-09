package com.example.alarmboss.alarm

import android.content.Context

/**
 * A record of which alarms are ringing right now, kept in storage rather than in memory. If the
 * system kills the app mid-ring (low memory, a crash, a vendor task killer) the service is restarted
 * with no memory of what it was doing; without this record the alarm would simply stop and the
 * challenge that was supposed to wake the user would never be shown.
 */
object RingingLog {

    /** Past this age a ring is treated as abandoned, so a stale record can't ring hours later. */
    const val MAX_AGE_MS = 30 * 60_000L

    data class Entry(val alarmId: Long, val followUp: String?, val startedAt: Long)

    fun encode(entry: Entry): String = "${entry.alarmId}|${entry.followUp.orEmpty()}|${entry.startedAt}"

    /** Reads stored entries, silently dropping anything malformed. */
    fun decode(raw: Set<String>?): List<Entry> = raw.orEmpty().mapNotNull { text ->
        val parts = text.split("|")
        if (parts.size != 3) return@mapNotNull null
        val id = parts[0].toLongOrNull() ?: return@mapNotNull null
        val at = parts[2].toLongOrNull() ?: return@mapNotNull null
        Entry(id, parts[1].ifEmpty { null }, at)
    }

    /** Entries still worth resuming at [now], oldest first. An entry stamped in the future is stale too. */
    fun fresh(entries: List<Entry>, now: Long): List<Entry> =
        entries.filter { it.startedAt in (now - MAX_AGE_MS)..now }.sortedBy { it.startedAt }

    /** [entries] with [entry] added, replacing any earlier record of the same ring. */
    fun plus(entries: List<Entry>, entry: Entry): List<Entry> =
        entries.filterNot { it.alarmId == entry.alarmId && it.followUp == entry.followUp } + entry

    fun minus(entries: List<Entry>, alarmId: Long, followUp: String?): List<Entry> =
        entries.filterNot { it.alarmId == alarmId && it.followUp == followUp }
}

/** The [RingingLog] kept in SharedPreferences. Written synchronously: the app may die right after. */
class RingingStore(context: Context) {
    private val prefs = context.getSharedPreferences("alarm_boss_ringing", Context.MODE_PRIVATE)

    fun entries(): List<RingingLog.Entry> = RingingLog.decode(prefs.getStringSet(KEY, null))

    /** Rings that should be resumed now. */
    fun resumable(now: Long = System.currentTimeMillis()): List<RingingLog.Entry> =
        RingingLog.fresh(entries(), now)

    fun add(alarmId: Long, followUp: String?, now: Long = System.currentTimeMillis()) =
        write(RingingLog.plus(entries(), RingingLog.Entry(alarmId, followUp, now)))

    fun remove(alarmId: Long, followUp: String?) = write(RingingLog.minus(entries(), alarmId, followUp))

    fun clear() = write(emptyList())

    private fun write(entries: List<RingingLog.Entry>) {
        prefs.edit().putStringSet(KEY, entries.map(RingingLog::encode).toSet()).commit()
    }

    private companion object {
        const val KEY = "ringing"
    }
}
