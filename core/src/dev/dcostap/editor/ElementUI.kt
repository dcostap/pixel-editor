package dev.dcostap.editor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.Matrix3
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.DelayedRemovalArray
import com.badlogic.gdx.utils.SnapshotArray
import dev.dcostap.Drawer2D
import dev.dcostap.Transform
import dev.dcostap.Transformable
import dev.dcostap.utils.ReadOnlyGdxArray

open class ElementUI : Transformable by Transform() {
	var color = Color.WHITE
	var alpha = 1f

	private val children = DelayedRemovalArray<ElementUI>()

	fun add(elementUI: ElementUI) {
		children.add(elementUI)
		elementUI.parent = this
	}

	fun remove() {
		parent?.let {
			it.children.removeAll { it === this }
		}
		parent = null
	}

	var parent: ElementUI? = null

	open fun update(delta: Float) {
		for (child in children)
			child.update(delta)
	}

	private var transform = Matrix4()
	private var affine = Affine2()
	private var oldTransform = Matrix4()

	private fun calculateTransform() {
		affine.setToTrnRotScl(x + origin.x, y + origin.y, rotation, scale.x, scale.y)
		affine.translate(origin.x, origin.y)

		parent?.affine?.let {
			affine.preMul(it)
		}

		transform.set(affine)
	}

	open fun draw(drawer: Drawer2D, parentAlpha: Float) {
		drawer.reset()

		drawer.alpha *= parentAlpha * this.alpha

		calculateTransform()

		oldTransform.set(drawer.batch.transformMatrix)
		drawer.batch.transformMatrix = transform

		drawSelf(drawer)

		drawChildren(drawer, parentAlpha)

		drawer.batch.transformMatrix = oldTransform
	}

	open fun drawSelf(drawer: Drawer2D) {
		drawer.drawRectangle(0f, 0f, 40f, 40f, true)
	}

	open fun drawChildren(drawer: Drawer2D, parentAlpha: Float) {
		for (child in children) {
			child.draw(drawer, parentAlpha)
		}
	}
}