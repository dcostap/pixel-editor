package dev.dcostap

import com.badlogic.gdx.Gdx
import dev.dcostap.utils.DebugLogger

object Debug {
	private val debugLog = DebugLogger(9999)
	fun log(text: Any? = "", type: DebugLogger.Type = DebugLogger.Type.INFO, origin: String = "") {
		debugLog.log(text, type, origin)
	}

	fun logBug(text: Any? = "", origin: String = "") {
		debugLog.log(text, DebugLogger.Type.BUG, origin)
	}

	fun logWarning(text: Any?, origin: String = "") {
		debugLog.log(text, DebugLogger.Type.WARNING, origin)
	}

	fun saveDebugLog(name: String = "debugLog.txt") {
		Gdx.files.local(name).writeString(debugLog.toString(), false)
	}
}