package dev.dcostap.utils.actions

import com.badlogic.gdx.math.Interpolation
import dev.dcostap.utils.Easing
import ktx.collections.*

typealias GdxActions = com.badlogic.gdx.scenes.scene2d.actions.Actions

/**
 * Created by Darius on 15/04/2018.
 */
abstract class Action @JvmOverloads constructor(duration: Float, interpolation: Interpolation = Interpolation.linear) {
	val interpolator = Interpolator(interpolation, duration)
	var pauseTimer = false
	private var finalUpdateDone = false

	fun baseUpdate(delta: Float) {
		if (finalUpdateDone) return

		fun checkFinished(): Boolean {
			if (getFinished()) {
				finalUpdate()
				finalUpdateDone = true
				return true
			}
			return false
		}

		if (checkFinished()) return

		update(delta)

		checkFinished()
	}

	protected open fun update(delta: Float) {
		if (!pauseTimer) interpolator.update(delta)
		updateProgress(interpolator.getPercentValue(), delta)
	}

	/** @param percent Progress from 0 to 1 (completed) affected by interpolation.
	 * Note that interpolation is already applied, and progress may go above 1 */
	abstract fun updateProgress(percent: Float, delta: Float)

	/** Always runs before finishing, even if the duration is 0 */
	abstract fun finalUpdate()

	fun forceFinish() {
		interpolator.timer.elapsed = interpolator.timer.timeLimit
	}

	/** Prepares the Action to be reused, reset all variables */
	open fun reset() {
		interpolator.timer.elapsed = 0f
		finalUpdateDone = false
	}

	val hasFinished: Boolean
		get() = finalUpdateDone

	protected open fun getFinished() = interpolator.hasFinished

	//region Actions
	/**
	 * Created by Darius on 04/05/2018.
	 *
	 * Interpolates a number from one initial value to an end value during the duration of the Action.
	 * Very versatile, attach a function and do whatever you want to the value
	 */
	class Value(val startValue: Float, val endValue: Float, duration: Float,
				interpolation: Interpolation, val valueUpdated: (Float) -> Unit = {})
		: Action(duration, interpolation) {
		override fun updateProgress(percent: Float, delta: Float) {
			val value = ((endValue - startValue) * percent) + startValue

			valueUpdated(value)
		}

		override fun finalUpdate() {
			valueUpdated(endValue)
		}
	}

	/** Unlike [Value], this one stores the values as lambda and stores their return value only once this Action starts running */
	class DeferredValue(val startValue: () -> Float, val endValue: () -> Float, duration: Float,
						interpolation: Interpolation, val valueUpdated: (Float) -> Unit = {})
		: Action(duration, interpolation) {
		private var firstTime = true
		private var actualStartValue: Float = 0f
		private var actualEndValue: Float = 0f

		override fun updateProgress(percent: Float, delta: Float) {
			if (firstTime) {
				firstTime = false

				actualStartValue = startValue()
				actualEndValue = endValue()
			}
			val value = ((actualEndValue - actualStartValue) * percent) + actualStartValue

			valueUpdated(value)
		}

		override fun finalUpdate() {
			if (firstTime) {
				firstTime = false

				actualStartValue = startValue()
				actualEndValue = endValue()
			}
			valueUpdated(actualEndValue)
		}
	}

	class ValueSequence private constructor() : Action(0f) {
		private val values = GdxArray<Value>()
		private var value = 0f
		private var valueUpdated: (Float) -> Unit = {}

		private var startValue = 0f
		private var newStartValue = false
		fun startWith(value: Float) {
			startValue = value
			newStartValue = true
		}

		private var durationScale = 1f

		fun scaleDurationBy(scale: Float) {
			this.durationScale = scale
		}

		private val currentStartValue: Float
			get() {
				return when {
					values.isEmpty -> {
						newStartValue = false
						startValue
					}
					!newStartValue -> {
						values.last().endValue
					}
					else -> {
						newStartValue = false
						startValue
					}
				}
			}

		private var defaultEasing = Easing.linear


		fun change(endValue: Float, duration: Float, easing: Easing = defaultEasing) {
			values.add(Value(currentStartValue, endValue, duration, easing) { value = it })
		}

		fun defaultEasing(easing: Easing) {
			this.defaultEasing = easing
		}

		fun wait(duration: Float) {
			values.add(Value(currentStartValue, currentStartValue, duration, Easing.linear) { value = it })
		}

		fun updateValue(f: (Float) -> Unit) {
			valueUpdated = f
		}

		override fun updateProgress(percent: Float, delta: Float) {}

		override fun finalUpdate() {}

		override fun reset() {
			index = 0
		}

		var index = 0

		override fun update(delta: Float) {
			if (hasFinished) return

			val delta = delta / durationScale

			values[index].baseUpdate(delta)
			if (values[index].hasFinished) index++

			valueUpdated(value)
		}

		override fun getFinished() = index == values.size

		companion object {
			fun new(init: ValueSequence.() -> Unit): ValueSequence {
				return ValueSequence().also(init)
			}
		}
	}

	class Sequence(vararg val actions: Action) : Action(0f) {
		override fun updateProgress(percent: Float, delta: Float) {}

		override fun finalUpdate() {}

		override fun reset() {
			index = 0
		}

		var index = 0

		override fun update(delta: Float) {
			if (hasFinished) return
			actions[index].baseUpdate(delta)
			if (actions[index].hasFinished) index++
		}

		override fun getFinished() = index == actions.size
	}

	class Parallel(vararg val actions: Action) : Action(0f) {
		override fun updateProgress(percent: Float, delta: Float) {}
		override fun finalUpdate() {}
		override fun reset() {}

		var finished = 0
		override fun update(delta: Float) {
			if (hasFinished) return

			finished = 0
			for (action in actions) {
				action.baseUpdate(delta)
				if (action.hasFinished) finished++
			}
		}

		override fun getFinished() = finished == actions.size
	}

	open class RunAfter(delay: Float = 0f, val function: () -> Unit) : Action(delay) {
		override fun updateProgress(percent: Float, delta: Float) {}

		override fun finalUpdate() {
			function()
		}
	}

	open class Run(val function: () -> Unit) : Action(0f) {
		override fun updateProgress(percent: Float, delta: Float) {}

		override fun finalUpdate() {
			function()
		}
	}

	open class RunUntil(val delay: Float = 0f, val function: (Float) -> Boolean) : Action(0f) {
		override fun updateProgress(percent: Float, delta: Float) {}

		private var elapsed = 0f

		private var finish = false
		override fun update(delta: Float) {
			if (hasFinished) return

			if (elapsed >= delay) {
				finish = function(delta)
			} else
				elapsed += delta
		}

		override fun getFinished() = finish

		override fun finalUpdate() {}
	}

	open class RepeatLoopAction(val newLoop: () -> Action?) : Action(0f) {
		override fun updateProgress(percent: Float, delta: Float) {}

		var action = newLoop()

		private var finished = false

		override fun update(delta: Float) {
			action?.baseUpdate(delta)
			if (action?.hasFinished == true) {
				action = newLoop()
			}
			if (action == null) finished = true
		}

		override fun getFinished() = finished

		override fun finalUpdate() {}
	}

	open class RunForever(delay: Float = 0f, update: (Float) -> Unit)
		: RunUntil(delay, { update(it); false })

	open class TriggerWhen(val condition: () -> Boolean, val trigger: () -> Unit) : Action(0f) {
		override fun updateProgress(percent: Float, delta: Float) {}

		private var finish = false
		override fun update(delta: Float) {
			if (hasFinished) return
			finish = condition()
		}

		override fun getFinished() = finish

		override fun finalUpdate() {
			trigger()
		}
	}

	class Repeat(times: Int, function: () -> Unit) : Run({ repeat(times) { function() } })

	class RunFor(val time: Float = 1f, val function: (delta: Float, elapsed: Float) -> Unit) : Action(0f) {
		override fun updateProgress(percent: Float, delta: Float) {

		}

		private var elapsed = 0f
		private var finish = false
		override fun update(delta: Float) {
			if (hasFinished) return

			if (elapsed >= time) {
				finish = true
			} else {
				elapsed += delta
				function(delta, elapsed)
			}
		}

		override fun getFinished() = finish

		override fun finalUpdate() {}
	}

	class Wait(delay: Float = 0f) : Action(delay) {
		override fun updateProgress(percent: Float, delta: Float) {}
		override fun finalUpdate() {}
	}

	companion object {
		fun dsl(init: ActionsUpdater.ActionDSL.() -> Unit): Sequence {
			return kotlin.run {
				ActionsUpdater.ActionDSL().let {
					it.init()
					Sequence(*it.build())
				}
			}
		}
	}

	//endregion
}
