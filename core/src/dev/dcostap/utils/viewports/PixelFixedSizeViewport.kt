package dev.dcostap.utils.viewports

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.Scaling
import com.badlogic.gdx.utils.viewport.ScalingViewport
import kotlin.math.max
import kotlin.math.min

/** Viewport with a fixed size. Camera will never see more than that unless you change the zoom.
 *
 * @param avoidDistortingPixels if true, letterboxing (black bars) will be used whenever the scale factor isn't integer */
class PixelFixedSizeViewport(val pixelsWidth: Float, val pixelsHeight: Float, camera: Camera? = OrthographicCamera(),
							 var avoidDistortingPixels: Boolean = true, var PPU: Int = 1) : ScalingViewport(Scaling.fit, 0f, 0f, camera) {
	override fun update(screenWidth: Int, screenHeight: Int, centerCamera: Boolean) {
		if (avoidDistortingPixels) {
			val factor = max(min(screenHeight / pixelsHeight, screenWidth / pixelsWidth), 1f)
			worldWidth = pixelsWidth * factor.toInt()
			worldHeight = pixelsHeight * factor.toInt()

			val viewportWidth = Math.round(worldWidth)
			val viewportHeight = Math.round(worldHeight)

			worldWidth /= PPU * factor.toInt()
			worldHeight /= PPU * factor.toInt()

			// Center.
			setScreenBounds((screenWidth - viewportWidth) / 2, (screenHeight - viewportHeight) / 2, viewportWidth, viewportHeight)

			apply(centerCamera)
		} else {
			worldWidth = pixelsWidth
			worldHeight = pixelsHeight
			scaling = Scaling.fit
			worldWidth /= PPU
			worldHeight /= PPU

			super.update(screenWidth, screenHeight, centerCamera)
		}
	}
}