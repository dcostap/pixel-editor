package dev.dcostap.utils

import com.badlogic.gdx.utils.JsonValue

/**
 * Created by Darius on 03/01/2018
 */
open class Timer @JvmOverloads constructor(var timeLimit: Float = 1f, isTimerOn: Boolean = true,
										   var turnOffWhenTimeIsReached: Boolean = false) : Saveable {
	var elapsed: Float = 0f
	var isTimerOn: Boolean = isTimerOn
		private set

	/**
	 * @return whether the timer reached the timeLimit
	 */
	fun tick(delta: Float): Boolean {
		if (isTimerOn) {
			elapsed += delta
			if (elapsed > timeLimit) {
				elapsed -= timeLimit

				if (turnOffWhenTimeIsReached) {
					turnOff()
				}

				return true
			}
		}

		return false
	}

	val progressPercent get() = timeLimit / elapsed

	fun reset() {
		this.elapsed = 0f
	}

	fun turnOff() {
		isTimerOn = false
	}

	fun turnOn() {
		isTimerOn = true
	}

	override fun save(): JsonValue {
		return JsonSavedObject().also {
			it.saveValues(elapsed, isTimerOn, turnOffWhenTimeIsReached, timeLimit)
		}
	}

	override fun load(jsonValue: JsonValue, saveVersion: String) {
		val data = jsonValue as JsonSavedObject
		elapsed = data.loadAnotherValue(elapsed)
		isTimerOn = data.loadAnotherValue(isTimerOn)
		turnOffWhenTimeIsReached = data.loadAnotherValue(turnOffWhenTimeIsReached)
		timeLimit = data.loadAnotherValue(timeLimit)
	}
}

open class LoopTimer @JvmOverloads constructor(isTimerOn: Boolean = true, private var getNextTimeLimit: () -> Float) {
	constructor(timeLimits: FloatArray, isTimerOn: Boolean = true) : this(isTimerOn, { timeLimits.random() })

	constructor(timeLimit: Float, isTimerOn: Boolean = true) : this(isTimerOn, { timeLimit })

	var elapsed: Float = 0f
	var isTimerOn: Boolean = isTimerOn
		private set

	var timeLimit: Float = getNextTimeLimit()
		set(value) {
			getNextTimeLimit = { value }
			field = getNextTimeLimit()
		}

	/**
	 * @return whether the timer reached the timeLimit
	 */
	fun tick(delta: Float): Boolean {
		if (isTimerOn) {
			elapsed += delta
			if (elapsed > timeLimit) {
				elapsed -= timeLimit

				timeLimit = getNextTimeLimit()
				return true
			}
		}

		return false
	}

	val progressPercent get() = elapsed / timeLimit

	fun reset() {
		this.elapsed = 0f
	}

	fun turnOff() {
		isTimerOn = false
	}

	fun turnOn() {
		isTimerOn = true
	}
}

/** Only ticks when the timeLimit is reached, then it turns off */
class AlarmTimer(timeLimit: Float, isTimerOn: Boolean = true) : Timer(timeLimit, isTimerOn, true)
