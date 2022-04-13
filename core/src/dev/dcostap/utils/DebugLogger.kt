package dev.dcostap.utils

import java.text.SimpleDateFormat
import java.util.*

var MUTE_LOGGING = false

/** Created by Darius on 07-Nov-18. */
class DebugLogger(storeLimit: Int = 9999) {
	var log = Stack<String>()
	private var printIt = true
		get() = field && !MUTE_LOGGING

	init {
		log.setSize(storeLimit)
	}

	private var warningRepetitionTimes = 0
	private var warningRepetitionLastDate = ""
	private var warningRepetitionLastBody = ""
	private var lastMinute = -1

	private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS")
	fun log(text: Any? = "\n", type: Type = Type.INFO, origin: String = "") {
		val date = Date()
		val time = dateFormat.format(date)
		val minutes = date.minutes
		val text = text.toString()

		val newString =
				if (text.isNullOrBlank())
					"\n"
				else
					(if (minutes != lastMinute && lastMinute >= 0) "\n" else "") +
							"$time  ${type.name} ${if (origin.isNotBlank()) "[${origin}]" else ""}> $text"

		lastMinute = minutes

		if (type == Type.WARNING && text == warningRepetitionLastBody) {
			warningRepetitionTimes++
			warningRepetitionLastDate = dateFormat.format(Date())
		} else {
			if (type == Type.WARNING) warningRepetitionLastBody = text

			if (warningRepetitionTimes > 0) {
				log.push(warningRepetitionLastDate + "     (Warning repeated $warningRepetitionTimes " +
						"time${if (warningRepetitionTimes > 1) "s" else ""})")
				warningRepetitionTimes = 0

				if (printIt) {
					println(log.last())
				}
				warningRepetitionLastBody = ""
			}

			log.push(newString)

			if (printIt) {
				println(newString)
			}
		}
	}

	override fun toString(): String {
		var string = ""
		for (str in log) {
			string += str ?: ""
		}
		return string
	}

	enum class Type {
		/** General messages that should not appear too much. The logger will try to avoid compacting them */
		INFO,
		BUG,
		/** Messages that may appear too much during gameplay, so the logger will try to compact them */
		WARNING
	}
}