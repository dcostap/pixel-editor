package dev.dcostap.editor

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import dev.dcostap.Drawer2D

class ImageUI(texture: TextureRegion) : ElementUI() {
	var texture = texture
		set(value) {
			field = value
			updateTexture()
		}

	init {
		updateTexture()
	}

	private fun updateTexture() {
		size.set(0f, 0f, texture.regionWidth.toFloat(), texture.regionHeight.toFloat())
	}

	override fun drawSelf(drawer: Drawer2D) {
		super.drawSelf(drawer)

		drawer.draw(texture)
	}
}

open class NinePatchUI(width: Float, height: Float, val ninePatch: NinePatch) : ElementUI() {
	init {
		size.width = width
		size.height = height
	}

	override fun drawSelf(drawer: Drawer2D) {
		super.drawSelf(drawer)

		drawer.draw(ninePatch, width = size.width, height = size.height)
	}
}