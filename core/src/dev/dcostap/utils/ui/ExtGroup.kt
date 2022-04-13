package dev.dcostap.utils.ui

import com.badlogic.gdx.scenes.scene2d.Group
import dev.dcostap.utils.actions.ActionsUpdater

/** Created by Darius on 09-Jun-20. */
open class ExtGroup : Group() {
	var onUpdate: (delta: Float) -> Unit = {}
	val darenActions = ActionsUpdater()

	override fun act(delta: Float) {
		super.act(delta)

		darenActions.update(delta)
		onUpdate(delta)
	}
}