package dev.dcostap

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import dev.dcostap.Debug.log
import dev.dcostap.Debug.saveDebugLog
import dev.dcostap.editor.MainScreen
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

object Main : ApplicationListener {
	val isRelease = false

	var screen: Screen? = null
		set(value) {
			field?.hide()
			field?.dispose()
			field = value
			screen?.resize(Gdx.graphics.width, Gdx.graphics.height)
		}

	fun launchDesktopApp() {
		val config = Lwjgl3ApplicationConfiguration().apply {
			setWindowedMode(1200, 700)
			useVsync(true)
			setTitle("")
		}

		log("time: " + System.currentTimeMillis())
		log("default_locale: " + Locale.getDefault())
		log("default_charset: " + Charset.defaultCharset())
		log("default_encoding: " + System.getProperty("file.encoding"))
		log("java_version: " + System.getProperty("java.version"))
		log("os_arch: " + System.getProperty("os.arch"))
		log("os_name: " + System.getProperty("os.name"))
		log("os_version: " + System.getProperty("os.version"))

		Lwjgl3Application(this, config)
	}

	override fun create() {
		if (!isRelease)
			AssetsExporter.exportAssets()

		loadProjectVersion()
	}

	private fun onAssetsLoaded() {
		screen = MainScreen()
	}

	lateinit var projectName: String
		private set

	lateinit var projectDescription: String
		private set

	lateinit var projectVersion: String
		private set

	private var areAssetsLoaded = false

	var delta: Float = 0f

	override fun render() {
		delta = Gdx.graphics.deltaTime.coerceAtMost(1 / 30f)

		if (!areAssetsLoaded) {
			if (Assets2D.processAssetLoading()) {
				areAssetsLoaded = true
				onAssetsLoaded()
			}
		}

		try {
			screen?.render(delta)
		} catch (e: Exception) {
			if (isRelease)
				saveDebugLog(
					"crash_log-${
						SimpleDateFormat(
							"yyyyMMdd_HHmmss",
							Locale.ENGLISH
						).format(Calendar.getInstance().time)
					}.txt"
				)
			dispose()

			throw e
		}
	}

	override fun pause() {

	}

	override fun resume() {

	}

	override fun resize(width: Int, height: Int) {
		screen?.resize(width, height)
	}

	override fun dispose() {
		screen?.dispose()
	}

	private fun loadProjectVersion(): JsonValue? {
		return JsonReader().parse(Gdx.files.internal("version.json")).also {
			projectVersion = it.getString("version")
			projectName = it.getString("projectName")
			projectDescription = it.getString("description", "")
		}
	}
}
