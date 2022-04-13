package dev.dcostap.utils.ui

import com.badlogic.gdx.scenes.scene2d.ui.Table
import dev.dcostap.utils.actions.ActionsUpdater

/**
 * Created by Darius on 19/04/2018.
 *
 * Provides an updateFunction
 */
open class ExtTable : Table() {
	var deltaMult = 1f
	val darenActions = ActionsUpdater()

	var onUpdate: (delta: Float) -> Unit = {}

	override fun act(delta: Float) {
		val newDelta = delta * deltaMult
		super.act(newDelta)

		onUpdate(newDelta)

		darenActions.update(newDelta)
	}
}