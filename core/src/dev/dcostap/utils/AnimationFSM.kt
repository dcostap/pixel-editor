package dev.dcostap.utils

import com.badlogic.gdx.graphics.g2d.TextureRegion
import ktx.collections.*

/** Created by Darius on 21-Apr-20. */
class AnimationFSM<T : Enum<T>>(
		startingAnim: T,
		vararg anims: Pair<T, Animation<TextureRegion>>
) {
	class Anim<T>(val enum: T, val animation: Animation<TextureRegion>) {
		fun start() {
			animation.reset()
			animation.resume()
		}

		fun update(delta: Float) {
			animation.update(delta)
		}
	}

	var current: T = startingAnim
		set(value) {
			if (field != value) {
				field = value
				anims.get(field).start()
			}
		}

	fun getAnim() = anims[current].animation

	val anims = GdxMap<T, Anim<T>>()

	init {
		anims.forEach {
			this.anims[it.first] = Anim(it.first, it.second)
		}

		this.current = startingAnim
	}

	fun update(delta: Float): TextureRegion {
		anims.get(current).update(delta)
		return anims.get(current).animation.getFrame()
	}
}