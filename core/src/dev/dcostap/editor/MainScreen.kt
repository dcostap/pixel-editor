package dev.dcostap.editor

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.g2d.CpuSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dev.dcostap.Drawer2D
import dev.dcostap.utils.Utils
import dev.dcostap.utils.setXY
import dev.dcostap.utils.use
import ktx.collections.*
import ktx.math.minus
import java.lang.Math.sin

class MainScreen : ScreenAdapter() {
	val viewport = ScreenViewport()
	val camera get() = viewport.camera
	val batch = CpuSpriteBatch()
	val drawer = Drawer2D(batch)

	var elapsed = 0f

	val ui = ElementUI().also {
		it.x = 500f
		it.y = 400f

		it.add(object : ElementUI() {
			init {
				x = 60f
				y = 100f
			}

			override fun update(delta: Float) {
				super.update(delta)

				origin.x += sin(elapsed.toDouble()).toFloat()
				rotation += 5f
			}
		})
	}

	override fun render(delta: Float) {
		update(delta)
		draw(delta)
	}

	private fun update(delta: Float) {
		elapsed += delta
		ui.update(delta)
	}

	private fun draw(delta: Float) {
		Utils.clearScreen()

		drawer.reset()

		batch.projectionMatrix = camera.combined
		batch.use {
			drawer.batch = it
			ui.draw(drawer, 1f)
			ui.rotation += 1f
			ui.scale.setXY(sin(elapsed.toDouble()).toFloat())
		}
	}

	override fun resize(width: Int, height: Int) {
		viewport.update(width, height, true)
	}
}