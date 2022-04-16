package dev.dcostap.editor

import com.badlogic.gdx.graphics.g2d.NinePatch
import dev.dcostap.Drawer2D

class ButtonUI(width: Float, height: Float, ninePatch: NinePatch) : NinePatchUI(width, height, ninePatch) {
	override fun onTouchDown(): Boolean {
		print("click")
		return true
	}
}