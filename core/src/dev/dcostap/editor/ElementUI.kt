package dev.dcostap.editor

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.DelayedRemovalArray
import com.badlogic.gdx.utils.SnapshotArray
import dev.dcostap.Drawer2D
import dev.dcostap.Transform
import dev.dcostap.Transformable
import dev.dcostap.utils.ReadOnlyGdxArray

open class ElementUI : Transformable by Transform() {
	var color = Color.WHITE
	var alpha = 1f

	var depth = 0

	val size = Rectangle()

	val root: RootUI?
		get() = (parent as? RootUI) ?: parent?.root

	val children = DelayedRemovalArray<ElementUI>()

	private val tmpVec = Vector2()

	fun calculateAbsolutePosition(): Vector2 {
		tmpVec.set(position)
		parent?.let {
			tmpVec.add(it.calculateAbsolutePosition())
		}
		return tmpVec
	}

	fun calculateAbsoluteScale(): Vector2 {
		tmpVec.set(scale)
		parent?.let {
			tmpVec.add(it.calculateAbsoluteScale())
		}
		return tmpVec
	}

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
		sortChildren()
		for (child in children) child.update(delta)
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

	}

	open fun drawChildren(drawer: Drawer2D, parentAlpha: Float) {
		sortChildren()

		for (child in children) {
			child.draw(drawer, parentAlpha)
		}
	}

	open fun onTouchDown(): Boolean {
		return false
	}

	private fun sortChildren() {
		children.sort { e1, e2 -> e1.depth.compareTo(e2.depth) }
	}
}