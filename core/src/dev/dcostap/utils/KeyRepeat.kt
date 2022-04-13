package dev.dcostap.utils

import kotlin.math.min

/** Created by Darius on 09-Apr-20.
 *
 * Key repeat that triggers at controllable rates
 * @param keyRepeatTimes total delta time needed for 2nd trigger to happen, 3rd, etc */
class KeyRepeat(private val keyRepeatTimes: FloatArray) {
	private var index = 0
	private var wasKeyPressed = false
	private var frameTime = 0f

	fun update(keyIsPressed: Boolean, delta: Float): Boolean {
		var result = false
		if (keyIsPressed) {
			if (!wasKeyPressed) result = true

			frameTime += delta
			if (frameTime >= keyRepeatTimes[index]) {
				index = min(keyRepeatTimes.size - 1, index + 1)
				frameTime = 0f
				result = true
			}

			wasKeyPressed = true
		} else {
			index = 0
			frameTime = 0f
			wasKeyPressed = false
		}

		return result
	}
}