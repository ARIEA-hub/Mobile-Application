package com.example.alarmboss.counselor

/**
 * What the counselor knows without any network: sleep and wellness topics, and how to help with the
 * app itself. It is deliberately plain code with no Android dependency, so every question it should
 * handle can be covered by a unit test.
 *
 * Matching is by whole words and phrases, never substrings: the old keyword list refused to talk about
 * "can't sleep before my python exam" because it saw "python", and treated "decode" as the word "code".
 * A question that carries a sleep topic is answered even when it also mentions something unrelated.
 */
object SleepKnowledge {

    const val OUT_OF_CONTEXT =
        "i don't really follow that, but i'm right here if you want to chat about your sleep or winding down. if you have any sort of doubts about how to improve sleep hygeine, and be well rested, don't ever be shy to ask!"

    private const val CLOSING =
        "if you have any sort of doubts about how to improve sleep hygeine, and be well rested, don't ever be shy to ask!"

    private class Topic(val id: String, val terms: List<String>, val answer: (Question) -> String)

    /** A question normalised for matching. */
    class Question(raw: String) {
        val text: String = raw.lowercase().replace('’', '\'').replace('‘', '\'')
        val words: List<String> = text.split(Regex("[^a-z0-9']+")).filter { it.isNotEmpty() }
        private val joined = " " + words.joinToString(" ") + " "

        /**
         * True if the question contains [term]: a word, a phrase of words, or a word prefix ending in `*`
         * (so `stress*` matches "stressed" and "stressful").
         */
        fun has(term: String): Boolean = when {
            term.endsWith("*") -> term.dropLast(1).let { prefix -> words.any { it.startsWith(prefix) } }
            ' ' in term -> joined.contains(" $term ")
            else -> term in words
        }

        fun hasAny(vararg terms: String): Boolean = terms.any { has(it) }
    }

    /** The reply to [query]. Always returns something; never throws. */
    fun answer(query: String): String {
        val q = Question(query)
        if (q.words.isEmpty()) return OUT_OF_CONTEXT

        // Someone in distress comes before every other rule, however the message is phrased.
        if (CRISIS_TERMS.any { q.has(it) }) return CRISIS_REPLY

        smallTalk(q)?.let { return it }

        val best = TOPICS
            .map { topic -> topic to topic.terms.count { q.has(it) } }
            .filter { it.second > 0 }
            .maxByOrNull { it.second } // first topic wins a tie, so the list is ordered by priority
        if (best != null) return best.first.answer(q)

        if (OFF_TOPIC_TERMS.any { q.has(it) }) return OUT_OF_CONTEXT
        if (SLEEP_WORDS.any { q.has(it) }) return GENERAL_ADVICE
        return OUT_OF_CONTEXT
    }

    /** Greetings, thanks and good-nights, which only count when the message is short and nothing more. */
    private fun smallTalk(q: Question): String? {
        val short = q.words.size <= 6
        if (q.hasAny("slept well", "slept great", "slept good", "slept amazing", "slept like a baby", "feeling rested", "woke up fresh") &&
            !q.hasAny("can't", "cant", "not", "didn't", "never", "haven't", "havent", "hardly", "barely")
        ) {
            return "that's wonderful to hear! waking up genuinely rested is such a win. did you do anything differently last night, or was your mind just more at ease?"
        }
        if (!short) return null
        if (q.hasAny("what's up", "whats up", "sup", "how are you", "how're you", "how r you")) {
            return if (q.has("what's up") || q.has("whats up") || q.has("sup")) {
                "hey there! i'm doing well, thank you. how are you feeling today — well-rested, or has it been a bit tiring?"
            } else {
                "doing well and glad you're here. how is your day treating you so far, and how have you been sleeping lately?"
            }
        }
        if (q.words.first() in setOf("thanks", "thank", "thx", "ty", "cheers")) {
            return "you're always welcome. get some peaceful rest tonight, and $CLOSING"
        }
        if (q.hasAny("good night", "goodnight", "going to bed", "going to sleep", "off to bed", "night night")) {
            return "good night! dim those lights down, put your phone away, and rest well."
        }
        if (q.has("good morning")) {
            return "morning! grab a glass of water and get some direct sunlight in your eyes to kickstart the day."
        }
        if (q.words.first() in setOf("hi", "hello", "hey", "hiya", "yo", "howdy")) {
            return "hey there. hope your day hasn't been too draining. how has your sleep felt recently?"
        }
        return null
    }

    private val TOPICS: List<Topic> = listOf(
        Topic("anxiety", listOf("anxious", "anxiety", "worried", "worrying", "racing thoughts", "mind is racing", "racing mind", "overthinking", "stress*", "panic*", "nervous", "mind racing", "can't switch off", "cant switch off")) {
            "when anxiety or racing thoughts kick in at night, your brain treats bed like a threat instead of a sanctuary. try the 'brain dump': grab a physical notepad and write down every single thought or worry so your brain knows they're stored safely. then try 4-7-8 breathing: inhale 4s, hold 7s, exhale 8s to trigger your vagus nerve and slow your heart rate."
        },
        Topic("breathing", listOf("breathing", "meditation", "meditate", "mindfulness", "4-7-8", "relaxation", "body scan", "visualization", "visualisation", "box breathing")) {
            "slow breathing is one of the quickest ways to settle your body. try 4-7-8: breathe in for 4, hold for 7, breathe out slowly for 8, a few rounds. or do a body scan, moving your attention from your toes up to your head and softening each part as you go. if your mind wanders, that's normal; just gently come back."
        },
        Topic("low-mood", listOf("depress*", "low mood", "feeling down", "burnout", "burned out", "burnt out", "hopeless", "lonely", "overwhelmed")) {
            "that sounds heavy, and i'm glad you said it out loud. sleep and mood feed into each other, so a steady wake-up time, daylight and a little movement can help the sleep side. if the low feeling has lasted more than a couple of weeks, or it's getting in the way of daily life, please talk to a doctor or a counselor, as you deserve real support beyond what i can give."
        },
        Topic("insomnia", listOf("can't sleep", "cant sleep", "cannot sleep", "trouble sleeping", "trouble falling asleep", "can't fall asleep", "cant fall asleep", "hard to fall asleep", "difficulty sleeping", "insomnia", "lying awake", "tossing and turning", "wide awake", "sleepless", "not sleepy", "can't get to sleep", "cant get to sleep")) { q ->
            val ongoing = q.hasAny("weeks", "months", "every night", "chronic", "years", "for ages")
            "i hear you, and lying awake when you just want to rest is so exhausting. to help you figure this out, what feels like the main barrier tonight — is your mind racing with worries or anxiety, or is your body feeling physically restless or tense? let me know what you're experiencing." +
                if (ongoing) " and since this has been going on for a while, it's worth talking to a doctor, and asking about cbt-i, the most effective long-term treatment for insomnia." else ""
        },
        Topic("night-waking", listOf("wake up at night", "waking up at night", "wake in the night", "waking in the night", "middle of the night", "keep waking", "keep waking up", "wake up often", "wake up in the middle", "3am", "3 am", "2am", "2 am", "4am", "4 am", "wake up at 3", "wake up at 4", "wake up at 2")) {
            "waking briefly at night is normal; what matters is getting back to sleep. try not to check the clock, keep the lights low, and if you're still awake after about 20 minutes, get up and do something quiet in dim light until you feel sleepy again. lingering in bed awake teaches your brain to link it with being alert."
        },
        Topic("early-waking", listOf("wake up too early", "waking too early", "waking up too early", "wake up early", "early morning waking", "up before my alarm", "wake before my alarm", "wake up before my alarm")) {
            "waking well before you need to can come from stress, an early light in the room, alcohol or caffeine late in the day, or a body clock that has drifted. keep the room dark, hold your wake-up time steady, get bright light soon after you do get up, and keep a notepad for any worries that show up at dawn. if it's persistent, a doctor can look into it."
        },
        Topic("caffeine", listOf("caffeine", "coffee", "tea", "espresso", "energy drink", "energy drinks", "cola", "pre workout", "matcha", "red bull")) {
            "caffeine hangs around in your system a lot longer than you might think — its half-life is around 5 to 7 hours, so an afternoon coffee is still active in your brain at bedtime. try cutting off caffeine about 8 to 10 hours before sleep. if you want something warm in the evening, a warm chamomile or peppermint tea works nicely."
        },
        Topic("alcohol", listOf("alcohol", "wine", "beer", "nightcap", "whiskey", "vodka", "drinking", "drink before bed", "hangover")) {
            "alcohol can make you drowsy, but it fragments the second half of the night and cuts down deep, restorative sleep, so you wake up less rested even after a full night. try to finish your last drink at least 3 to 4 hours before bed, and have water alongside it."
        },
        Topic("screens", listOf("screen", "screens", "phone", "blue light", "tv", "laptop", "scrolling", "doomscrolling", "social media", "tablet", "netflix", "youtube", "gaming", "video games", "instagram", "tiktok")) {
            "screens blast blue light that tricks your brain into thinking it's still daytime, which puts the brakes on your natural melatonin production. try putting your phone across the room 30 minutes before bed and dim the lights down low. give your mind a quiet buffer to unplug."
        },
        Topic("exercise", listOf("exercis*", "workout", "workouts", "gym", "running", "yoga", "stretching", "training", "cardio", "lifting")) {
            "regular movement is one of the best things for sleep. hard workouts are best finished 2 to 3 hours before bed, since they raise your body temperature and heart rate, while gentle stretching or yoga in the evening can help you wind down. morning or afternoon activity tends to work best, and it deepens your sleep that night."
        },
        Topic("naps", listOf("nap", "naps", "napping", "power nap", "siesta", "daytime sleep", "afternoon sleep")) {
            "naps can give you a solid reset if you keep them around 20 to 25 minutes. anything longer pushes you into deep sleep, leaving you feeling groggier when you wake up. also try to wrap up naps before 3 PM so you don't mess with your sleep drive at night."
        },
        Topic("light", listOf("sunlight", "daylight", "morning light", "bright light", "circadian", "body clock", "internal clock", "sunrise", "light exposure")) {
            "light is the main signal that sets your body clock. getting 10 to 15 minutes of outdoor light soon after you wake, even on a cloudy day, anchors your rhythm and makes you sleepier at night. in the evening do the opposite: lower the lights and keep the room dim before bed."
        },
        Topic("temperature", listOf("temperature", "too hot", "too cold", "hot room", "cold room", "warm room", "cool room", "room too warm", "room too hot", "sweating", "night sweats", "overheating", "heater", "air conditioning")) {
            "your core body temperature naturally needs to drop by about 1 to 2 degrees Fahrenheit for deep sleep to kick in. keeping your bedroom cool — around 65°F to 68°F (18°C) — makes a massive difference. a warm shower before bed actually helps because it brings heat to the surface of your skin to dump it faster."
        },
        Topic("environment", listOf("dark", "darkness", "noise", "noisy", "blackout", "earplugs", "white noise", "loud", "neighbors", "neighbours", "streetlight", "light in my room", "traffic")) {
            "a sleep space works best when it's dark, quiet and cool. blackout curtains or a sleep mask block stray light, and earplugs or steady white noise soften sudden sounds that pull you out of light sleep. cover small standby lights too, since even a little light can lighten your sleep."
        },
        Topic("comfort", listOf("mattress", "pillow", "pillows", "sheets", "blanket", "duvet", "back pain", "neck pain", "stiff neck", "uncomfortable bed", "sleeping position", "side sleeper", "stomach sleeper")) {
            "comfort matters more than it sounds. a pillow that keeps your neck level with your spine and a mattress that supports without feeling hard can cut night-time aches. side sleepers often like a pillow between the knees, back sleepers a small one under them. if pain is waking you regularly, it's worth getting it checked."
        },
        Topic("physical", listOf("restless", "tension", "tense", "pain", "ache", "aches", "tightness", "sore", "muscle*", "agitated", "can't relax my body", "cant relax my body")) {
            "physical restlessness or tension often happens when our muscles stay contracted from daytime stress. try progressive muscle relaxation: starting at your toes, squeeze firmly for 5 seconds, then fully release and feel the warmth. works wonders for easing physical agitation."
        },
        Topic("restless-legs", listOf("restless legs", "restless leg", "legs twitch", "twitching", "leg cramps", "cramps", "creepy crawly", "urge to move my legs")) {
            "an urge to move your legs that eases when you move them, mostly in the evening, can be restless legs syndrome. gentle stretching, a warm bath, and cutting back on caffeine and alcohol can help, and low iron is a common cause, so it's worth mentioning to a doctor for a simple check."
        },
        Topic("snoring-apnea", listOf("snor*", "sleep apnea", "apnea", "gasping", "stop breathing", "stops breathing", "choking", "pauses in breathing", "wake up gasping")) {
            "loud snoring with pauses in breathing, gasping or choking at night, or feeling exhausted all day despite enough hours, can point to sleep apnea. it's common and very treatable, so please mention it to a doctor rather than trying to fix it with habits alone. sleeping on your side and avoiding alcohol close to bed can help in the meantime."
        },
        Topic("nightmares", listOf("nightmare*", "dream*", "vivid dreams", "bad dreams", "night terrors", "sleep paralysis", "sleepwalking", "sleep talking")) {
            "dreams, including vivid or strange ones, mostly happen in rem sleep, and they tend to be more intense when you're stressed, overtired or sleeping irregularly. for recurring nightmares, a calm wind-down, writing the dream down in the morning and imagining a different ending while awake can help. if they're frequent or distressing, a doctor or therapist can help."
        },
        Topic("supplements", listOf("melatonin", "supplement", "supplements", "sleeping pill", "sleeping pills", "medication", "medicine", "pills", "magnesium", "valerian", "cbd", "benadryl", "antihistamine", "zolpidem", "ambien", "sleep aid", "sleep aids")) {
            "supplements and sleeping pills can help some people in the short term, but they work best alongside good habits, and the dose and timing matter. it's worth checking with a doctor or pharmacist first, especially if you take other medication, are pregnant, or are under 18. i'm happy to help with the habits side."
        },
        Topic("food", listOf("dinner", "late dinner", "snack", "snacking", "hungry", "heartburn", "reflux", "eating", "meal", "meals", "thirsty", "bathroom", "pee", "peeing", "water before bed", "midnight snack")) {
            "a heavy meal close to bedtime can keep your digestion working when it should be winding down, so aim to finish dinner two to three hours before bed. if you're hungry later, a light snack with some protein or complex carbs is fine. cutting back on fluids in the last hour helps with night-time bathroom trips, and propping your head up a little can ease reflux."
        },
        Topic("duration", listOf("how many hours", "how much sleep", "hours of sleep", "enough sleep", "sleep need", "sleep needs", "recommended sleep", "eight hours", "8 hours", "7 hours", "6 hours", "5 hours", "4 hours", "sleep duration", "ideal sleep")) {
            "most adults do best on 7 to 9 hours, teenagers on 8 to 10, and school-age children on 9 to 12. it's less about hitting a magic number and more about waking up mostly refreshed without needing to be dragged out of deep sleep. if you consistently feel drained after a full night, it's worth mentioning to a doctor."
        },
        Topic("sleep-debt", listOf("sleep debt", "catch up", "catching up", "not enough sleep", "sleep deprived", "sleep deprivation", "lack of sleep", "all nighter", "all-nighter", "short on sleep", "only slept", "slept only", "exam week", "burning the candle")) {
            "sleep debt is real, but you can't repay it in one long lie-in. the gentler fix is to go to bed 30 to 60 minutes earlier for a few nights and keep your wake-up time steady, instead of sleeping in for hours, which shifts your body clock and makes monday harder."
        },
        Topic("shift-work", listOf("jet lag", "jetlag", "shift work", "night shift", "night shifts", "time zone", "time zones", "rotating shifts", "working nights", "long haul")) {
            "shifting your clock is hard, so make the changes gradual and use light as your tool. for a new time zone, get bright light at the local morning and avoid it at your body's old morning, moving bedtime 1 hour a day if you can. for night shifts, wear sunglasses on the way home, sleep in a dark cool room, and keep the same sleep schedule on days off as much as you can."
        },
        Topic("schedule", listOf("schedule", "irregular", "routine", "consistent", "consistency", "same time", "bedtime", "weekend", "weekends", "sleep pattern", "sleep cycle", "night owl", "early bird", "sleep in")) {
            "your body loves consistency more than anything else. the biggest anchor isn't even bedtime — it's when you wake up. try getting out of bed at the exact same hour every day, even on weekends, and catch 10 minutes of direct morning sunlight to lock in your internal clock."
        },
        Topic("wind-down", listOf("wind down", "winding down", "relax", "relaxing", "bedtime routine", "routine before bed", "calm down", "can't relax", "cant relax", "read a book", "bath", "shower", "before bed", "pre sleep")) {
            "a wind-down routine tells your brain the day is over. try the same 30 to 45 minutes each night: dim the lights, put screens away, and pick something calm, such as a warm shower, light stretching, reading a paper book or a few slow breaths. what matters is repeating it, so choose things you actually enjoy."
        },
        Topic("grogginess", listOf("groggy", "grogginess", "inertia", "sleep inertia", "tired in morning", "tired in the morning", "foggy", "brain fog", "sluggish", "fatigue", "drowsy", "tired all day", "always tired", "exhausted")) {
            "that heavy morning grogginess is just sleep inertia. two quick things help shake it off right away: drink a big glass of room-temperature water right when your alarm goes off to rehydrate, and open the blinds for bright light. moving your body with the AlarmBoss physical challenges kicks your neuromuscular system into gear too. if you're tired all day despite enough sleep, mention it to a doctor."
        },
        Topic("waking-up", listOf("oversleep", "oversleeping", "sleeping too much", "sleep too much", "hard to wake up", "hit snooze", "hitting snooze", "can't wake up", "cant wake up", "heavy sleeper", "sleep through my alarm", "sleep through alarms", "snooze", "get out of bed", "get up")) {
            "if mornings are a battle, treat the cause and the wake-up separately. a steady bedtime, bright light and water within minutes of waking help the cause, and a challenge alarm, such as alarmboss medium or strict mode, makes sure you actually get up. try to avoid snoozing: those extra ten minutes are light, fragmented sleep that tends to leave you groggier."
        },
        Topic("kids", listOf("my child", "my kid", "my kids", "my son", "my daughter", "teenager", "teen", "toddler", "baby", "school morning", "school mornings", "children", "bedtime for kids")) {
            "kids and teens need more sleep than adults, about 9 to 12 hours for school-age children and 8 to 10 for teenagers, and they do best with fixed bedtimes, screens out of the bedroom and a calm routine. parent mode in alarmboss can set a school-morning alarm with a locked wake-up check, so mornings run on their own."
        },
        Topic("app-modes", listOf("easy mode", "medium mode", "strict mode", "alarm mode", "alarm modes", "challenge", "challenges", "boss fight", "boss fights", "streak", "xp", "wake check", "wake-up check", "wake up check", "alarmboss", "parent mode", "templates", "backup alarm")) {
            "alarmboss has three modes. easy is a one-tap dismiss, medium asks you to solve a challenge like math, a pattern, reading or steps, and strict makes you do a workout or a memory puzzle. medium and strict alarms ring again a few minutes after you finish with a quick wake-up check. completing alarms earns xp and keeps your streak, and every seventh day of a streak brings a boss fight."
        },
        Topic("app-howto", listOf("set an alarm", "add an alarm", "create an alarm", "new alarm", "change alarm", "edit alarm", "delete alarm", "remove alarm", "how do i set", "how to set", "set my alarm")) {
            "tap the + button on the alarms tab, choose a time, repeat days and a mode, then save. you can also pick a ready-made routine from templates. an alarm that's about to ring within two hours is locked so it can't be changed or switched off, and the emergency options are there only for when you genuinely can't continue."
        },
        Topic("app-stuck", listOf("won't stop", "wont stop", "can't dismiss", "cant dismiss", "stuck", "can't turn off", "cant turn off", "emergency stop", "emergency")) {
            "if an alarm's challenge can't be completed, hold the red emergency stop button for 10 seconds at the bottom of the ringing screen. it silences the alarm but gives no streak or xp. a locked alarm in the list can be switched off by long-pressing it and holding to confirm."
        },
        Topic("pets", listOf("my dog", "my cat", "my pet", "my pets", "pets in bed", "dog in bed", "cat in bed", "dog on the bed", "cat on the bed", "puppy", "kitten")) {
            "sharing a bed with a pet can quietly break up your sleep through movement, noise and warmth. if you wake often, try giving them their own comfy spot in the room for a week or two and see how your nights change."
        }
    )

    /** Words that show a message is about sleep, for questions no specific topic covers. */
    private val SLEEP_WORDS = listOf(
        "sleep", "sleeping", "sleepy", "slept", "bed", "bedtime", "rest", "rested", "resting", "wake", "woke", "waking",
        "awake", "tired", "tiredness", "insomnia", "nap", "caffeine", "coffee", "melatonin", "groggy", "fatigue", "night",
        "tonight", "morning", "mornings", "alarm", "alarms", "hygiene", "snore", "pillow", "dream", "wellness", "relax",
        "routine", "schedule", "energy", "refreshed", "drowsy"
    )

    /** Clearly unrelated subjects, used only when nothing in the question is about sleep. */
    private val OFF_TOPIC_TERMS = listOf(
        "restaurant", "food delivery", "pizza", "burger", "code", "coding", "programming", "python", "kotlin", "java",
        "javascript", "sql", "weather", "forecast", "cricket", "football", "soccer", "nba", "movie", "movies", "film",
        "politics", "president", "election", "stock", "stocks", "crypto", "bitcoin", "capital of", "who is", "who won",
        "solve", "math", "calculator", "hotel", "shopping", "recipe", "translate", "homework", "essay"
    )

    private val CRISIS_TERMS = listOf(
        "suicide", "suicidal", "kill myself", "end my life", "want to die", "wanna die", "self harm", "hurt myself",
        "don't want to live", "dont want to live", "no reason to live", "better off dead"
    )

    private const val CRISIS_REPLY =
        "i'm really glad you told me, and i want you to be safe right now. please reach out to someone who can be with you: a local emergency number or crisis line, or someone you trust nearby, and tell them how bad it feels. you don't have to carry this alone, and a real person can help in ways i can't."

    private const val GENERAL_ADVICE =
        "good sleep really comes down to a few steady habits: a dark, cool room, winding down without screens, and keeping a steady wake-up time. $CLOSING"
}
