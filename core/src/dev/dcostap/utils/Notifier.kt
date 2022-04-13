package dev.dcostap.utils

import ktx.collections.*

/**
 * Created by Darius on 06/09/2017.
 *
 * Common class for all notifiers
 */
open class Notifier<T> {
	var listeners = GdxArray<T>()

	fun registerListener(listener: T) {
		listeners.add(listener)
	}

	fun removeListener(listener: T) {
		listeners.removeValue(listener, true)
	}

	fun notifyListeners(f: (T) -> Unit) {
		listeners.forEachWithoutIterator {
			f(it)
		}
	}
}