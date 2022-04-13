package dev.dcostap.utils

import com.badlogic.gdx.math.Vector2

/** [x], [y] start at 0. Increment these 2 however you want to advance the noise.
 * A random hidden offset initialized on creation is later added to position */
class Noise {
	var x: Float
		get() = position.x
		set(value) {
			position.x = value
		}

	var y: Float
		get() = position.y
		set(value) {
			position.y = value
		}

	val position = Vector2()
	private val startX = randomFloat(0f, 999999f)
	private val startY = randomFloat(0f, 999999f)

	var maxValue = 1f

	fun get(offsetX: Float = 0f, offsetY: Float = 0f) = simplexNoise.eval(startX + x + offsetX, startY + y + offsetY) * maxValue
}

val simplexNoise = dev.dcostap.utils.OpenSimplexNoise()