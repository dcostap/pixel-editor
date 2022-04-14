package dev.dcostap.utils.ui

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import dev.dcostap.Drawer2D

/**
 * Created by Darius on 06/04/2018.
 */
open class DrawingTable(stage: Stage, var onDraw: (self: DrawingTable) -> Unit = {}) : Table() {
	val gd = Drawer2D(stage.batch)

	override fun drawBackground(batch: Batch?, parentAlpha: Float, x: Float, y: Float) {
		super.drawBackground(batch, parentAlpha, x, y)
		gd.alpha = parentAlpha
		onDraw(this)
		draw(batch, parentAlpha, x, y)
	}

	open fun draw(batch: Batch?, parentAlpha: Float, x: Float, y: Float) {

	}
}


open class DrawingWidget(stage: Stage, var onDraw: (self: DrawingWidget) -> Unit = {}) : Actor() {
	val gd = Drawer2D(stage.batch)

	override fun draw(batch: Batch?, parentAlpha: Float) {
		super.draw(batch, parentAlpha)

		gd.alpha = parentAlpha
		onDraw(this)
		draw(batch, parentAlpha, x, y)
	}

	open fun draw(batch: Batch?, parentAlpha: Float, x: Float, y: Float) {

	}
}
