package dev.dcostap.utils.actions

import com.badlogic.gdx.math.Interpolation
import dev.dcostap.utils.Timer
import dev.dcostap.utils.map
import dev.dcostap.utils.mapPercent

/**
 * Created by Darius on 10/04/2018.
 */
class Interpolator(var interpolation: Interpolation, var duration: Float) {
	val timer = Timer(10000000f)

	/** @return the percentage (0f to 1f) of progress, with the interpolation applied, so may go above 1f */
	fun getPercentValue(): Float {
		return interpolation.apply(map(timer.elapsed, 0f, duration, 0f, 1f))
	}

	fun getValue(minValue: Number, maxValue: Number): Float {
		return mapPercent(getPercentValue(), minValue, maxValue, interpolation)
	}

	fun update(delta: Float) {
		timer.tick(delta)
	}

	fun resetElapsed() {
		timer.reset()
	}

	val hasFinished
		get() = timer.elapsed > duration

	companion object {
		/** @return the percentage (0f to 1f) of progress, with the interpolation applied, so may go above 1f */
		fun getPercentValue(interpolation: Interpolation, percentProgress: Float): Float {
			return interpolation.apply(percentProgress)
		}

		fun getValue(interpolation: Interpolation, percentProgress: Float, minValue: Number, maxValue: Number): Float {
			return ((maxValue.toFloat() - minValue.toFloat()) * getPercentValue(interpolation, percentProgress)) + minValue.toFloat()
		}
	}
}
