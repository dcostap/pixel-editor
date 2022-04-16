package dev.dcostap.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.g2d.CpuSpriteBatch
import com.badlogic.gdx.utils.viewport.ScreenViewport
import dev.dcostap.Assets2D
import dev.dcostap.Drawer2D
import dev.dcostap.utils.Utils
import dev.dcostap.utils.setXY
import dev.dcostap.utils.use
import java.lang.Math.sin

class MainScreen : ScreenAdapter() {
	val viewport = ScreenViewport()
	val camera get() = viewport.camera
	val batch = CpuSpriteBatch()
	val drawer = Drawer2D(batch)

	var elapsed = 0f

	val ui = RootUI(viewport)

	init {
		viewport.unitsPerPixel = 0.25f

		ui.let {
			it.add(ImageUI(Assets2D.getRegion("cursor")))

			it.add(ButtonUI(100f, 100f, Assets2D.getNinePatch("button")))
		}

//		ui.scale.setXY(3f)

		Gdx.input.inputProcessor = ui
	}

	override fun render(delta: Float) {
		update(delta)
		draw(delta)
	}

	private fun update(delta: Float) {
		elapsed += delta
		ui.update(delta)
	}

	companion object {
		var debugText = ""
	}

	private fun draw(delta: Float) {
		Utils.clearScreen()

		drawer.reset()

		batch.projectionMatrix = camera.combined
		batch.use {
			drawer.batch = it
			ui.draw(drawer, 1f)

			drawer.drawText(
				debugText,
				Assets2D.visSmallFont,
				Gdx.input.x.toFloat(),
				Gdx.graphics.height - Gdx.input.y.toFloat()
			)

			debugText = ""
		}
	}

	override fun resize(width: Int, height: Int) {
		viewport.update(width, height, true)
	}
}