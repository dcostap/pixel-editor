package dev.dcostap.utils.viewports

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.Viewport
import kotlin.math.max
import kotlin.math.min

/** Scales into the lowest integer, expands the view size if needed like ScreenViewport */
class PixelScreenViewport(var pixelsWidth: Int, var pixelsHeight: Int, camera: Camera? = OrthographicCamera())
	: Viewport() {
	init {
		setCamera(camera)
	}

	override fun update(screenWidth: Int, screenHeight: Int, centerCamera: Boolean) {
		val factor = max(min(screenHeight / pixelsHeight, screenWidth / pixelsWidth), 1)

		// todo make the calculations to allow limited extra "room to grow" in X and Y, and letterboxing after that
		val viewportWidth = max(pixelsWidth * factor, screenWidth)
		val viewportHeight = max(pixelsHeight * factor, screenHeight)

		worldWidth = (viewportWidth / factor).toFloat()
		worldHeight = (viewportHeight / factor).toFloat()

		// Center.
		setScreenBounds((screenWidth - viewportWidth) / 2, (screenHeight - viewportHeight) / 2, viewportWidth, viewportHeight)

		apply(centerCamera)
	}

	override fun apply(centerCamera: Boolean) {
//		HdpiUtils.glViewport(screenX, screenY, screenWidth, screenHeight)
		camera.viewportWidth = worldWidth
		camera.viewportHeight = worldHeight
		if (centerCamera) camera.position[worldWidth / 2, worldHeight / 2] = 0f
		camera.update()
	}
}