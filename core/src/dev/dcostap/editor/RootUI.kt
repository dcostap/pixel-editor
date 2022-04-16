package dev.dcostap.editor

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.Viewport
import dev.dcostap.utils.Utils
import ktx.collections.*

class RootUI(val viewport: Viewport) : ElementUI(), InputProcessor {
	private fun propagateEventsOnPosition(screenX: Int, screenY: Int, onHandle: (elementUI: ElementUI) -> Boolean): Boolean {
		val worldPos = Utils.projectPosition(screenX, screenY, null, viewport, flipY = false)

		MainScreen.debugText += worldPos.toString()

		val hits = gdxArrayOf<ElementUI>()
		fun exploreChildrenOf(elementUI: ElementUI) {
			for (child in elementUI.children) {
				val rect = Rectangle.tmp
				rect.set(child.size)

				child.calculateAbsolutePosition().let {
					rect.x += it.x
					rect.y += it.x
				}

				child.calculateAbsoluteScale().let {
					rect.width *= it.x
					rect.height *= it.y
				}

				println(rect)

				if (rect.contains(worldPos)) {
					hits.add(child)
				}

				exploreChildrenOf(child)
			}
		}

		exploreChildrenOf(this)


		hits.sort { e1, e2 -> e1.depth.compareTo(e2.depth) }
		for (hit in hits) {
			if (onHandle(hit)) return true
		}
		return false
	}

	override fun keyDown(keycode: Int): Boolean {
		return false
	}

	override fun keyUp(keycode: Int): Boolean {
		return false
	}

	override fun keyTyped(character: Char): Boolean {
		return false
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		return propagateEventsOnPosition(screenX, screenY) {
			it.onTouchDown()
		}
	}

	override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		return false
	}

	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
		return false
	}

	override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
		return false
	}

	override fun scrolled(amountX: Float, amountY: Float): Boolean {
		return false
	}
}