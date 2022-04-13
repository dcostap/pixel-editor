package dev.dcostap.utils.actions

import com.badlogic.gdx.utils.Array
import dev.dcostap.utils.Easing
import ktx.collections.*

/**
 * Created by Darius on 15/04/2018.
 */
class ActionsUpdater {
	private val actions = Array<Action>()
	private val dummy = Array<Action>()

	private val tags = GdxMap<String, Action>()

	val size
		get() = actions.size

	val isEmpty
		get() = actions.size == 0

	/**
	 * Allows to add actions with += operator.
	 */
	operator fun plusAssign(action: Action) = add(action)

	/**
	 * Allows to remove actions with -= operator.
	 */
	operator fun minusAssign(action: Action) = removeAction(action)

	fun update(delta: Float) {
		dummy.clear()

		for (action in actions) {
			action.baseUpdate(delta)

			if (action.hasFinished) {
				dummy.add(action)
			}
		}

		for (a in dummy) removeAction(a)
	}

	fun add(action: Action) {
		action.reset()
		actions.add(action)
	}

	/** Adds action but removes any other previous action with same tag
	 * @param tag Can be used to identify an Action by name. See [ActionsUpdater.hasTag] */
	fun addOrReplace(tag: String, action: Action) {
		action.reset()
		actions.add(action)
		tags.put(tag, action)?.let { actions.removeValue(it, true) }
	}

	fun addOrReplace(tag: String, vararg actions: Action) {
		addOrReplace(tag, Action.Sequence(*actions))
	}

	fun add(vararg actions: Action) {
		add(Action.Sequence(*actions))
	}

	fun addOrReplace(tag: String, init: ActionDSL.() -> Unit) {
		ActionDSL().also {
			it.init()
			addOrReplace(tag, *it.build())
		}
	}

	fun add(init: ActionDSL.() -> Unit) {
		ActionDSL().also {
			it.init()
			add(*it.build())
		}
	}

	/** Looks for tags in all actions inside, including actions inside sequences or parallel actions */
	fun hasTag(tag: String): Boolean {
		if (tags.contains(tag)) return true
		return false
	}

	fun removeAction(action: Action) {
		actions.removeValue(action, true)
		for (tag in tags) if (tag.value === action) {
			tags.remove(tag.key)
			break
		}
	}

	fun clearTag(tag: String) {
		if (tags.containsKey(tag))
			removeAction(tags[tag])
	}

	fun clear() {
		tags.clear()
		actions.clear()
	}

	class ActionDSL {
		val actionsArray = gdxArrayOf<Action>()
		internal fun build() = actionsArray.toArray()

		fun add(sequence: Action.Sequence) {
			actionsArray.add(sequence)
		}

		fun sequence(init: ActionDSL.() -> Unit) {
			ActionDSL().also {
				it.init()
				actionsArray.add(Action.Sequence(*it.build()))
			}
		}

		fun repeatLoop(init: ActionDSL.() -> Unit) {
			actionsArray.add(Action.RepeatLoopAction {
				val dsl = ActionDSL()
				dsl.init()
				Action.Sequence(*dsl.build())
			})
		}

		fun parallel(init: ActionDSL.() -> Unit) {
			ActionDSL().also {
				it.init()
				actionsArray.add(Action.Parallel(*it.build()))
			}
		}

		fun run(f: () -> Unit) {
			actionsArray.add(Action.Run(f))
		}

		fun wait(time: Float) {
			actionsArray.add(Action.Wait(time))
		}

		fun runFor(time: Float, f: (delta: Float, elapsed: Float) -> Unit) {
			actionsArray.add(Action.RunFor(time, f))
		}

		fun runAfter(delay: Float, function: () -> Unit) {
			actionsArray.add(Action.RunAfter(delay, function))
		}

		fun valueSequence(init: Action.ValueSequence.() -> Unit) {
			actionsArray.add(Action.ValueSequence.new(init))
		}

		fun value(startValue: Float, endValue: Float, duration: Float, easing: Easing = Easing.linear, valueUpdated: (Float) -> Unit = {}) {
			actionsArray.add(Action.Value(startValue, endValue, duration, easing, valueUpdated))
		}

		fun deferredValue(startValue: () -> Float, endValue: () -> Float, duration: Float, easing: Easing = Easing.linear, valueUpdated: (Float) -> Unit = {}) {
			actionsArray.add(Action.DeferredValue(startValue, endValue, duration, easing, valueUpdated))
		}

		fun runUntil(function: (Float) -> Boolean) {
			actionsArray.add(Action.RunUntil(function = function))
		}

		fun waitUntil(function: (Float) -> Boolean) {
			runUntil(function)
		}

		fun runForever(function: (Float) -> Unit) {
			actionsArray.add(Action.RunForever(update = function))
		}

		fun triggerWhen(condition: () -> Boolean, trigger: () -> Unit) {
			actionsArray.add(Action.TriggerWhen(condition, trigger))
		}

		fun repeat(times: Int, init: ActionDSL.() -> Unit) {
			val actions = gdxArrayOf<Action>()
			kotlin.repeat(times) {
				ActionDSL().also {
					it.init()
					actions.addAll(it.actionsArray)
				}
			}

			actionsArray.add(Action.Sequence(*actions.toArray()))
		}
	}
}
