package dev.dcostap.utils

import com.badlogic.gdx.math.MathUtils
import dev.dcostap.Assets2D
import ktx.collections.*

/**
 * Created by Darius on 17/01/2018
 */
open class Animation<out T>(frames: GdxArray<T>, framesPerSecond: Float, animType: AnimType = AnimType.LOOP) {
	constructor(frame: T, duration: Float = 1f) : this(GdxArray<T>().also { it.add(frame) }, 1f / duration)

//	constructor(framesAndDuration: GdxArray<Pair<T, Float>>, animType: AnimType = AnimType.LOOP)
//			: this(GdxArray(framesAndDuration.map { it.first }), 1f, animType) {
//		framesAndDuration.forEachIndexed { index, pair ->
//			customFrameDuration.put(index, pair.second)
//		}
//	}

	var elapsedTime = 0f
		private set

	var isPaused = false

	var animType: AnimType = animType
		set(value) {
			field = value
			loopsFinished = 0
		}

	val customFrameDuration = GdxMap<Int, Float>() // this feature seems to not work

	private var frames = frames
		set(value) {
			field = value
			updateTotalAnimationDuration()
		}

	var totalAnimationDuration: Float = 0f
		private set

	/** -1 = deactivate */
	var stopWhenFinishedThisNumberOfLoops = -1

	enum class AnimType {
		ONE_LOOP, LOOP, REVERSED, REVERSED_THEN_NORMAL, STOP_IN_EACH_NEW_FRAME
	}

	private var normalReversedCycle = AnimType.LOOP

	private fun getDecimalPart(number: Float): Float {
		return number - number.toInt()
	}

	fun changeFrameDurationKeepingFrameIndex(frameDuration: Float) {
		val frame = currentFrame
		val progress = getDecimalPart(elapsedTime / frameDuration)
		this.frameDuration = frameDuration
		this.elapsedTime = frame * frameDuration
		this.elapsedTime += frameDuration * progress
	}


	/** @see framesPerSecond */
	var frameDuration: Float
		set(value) {
			framesPerSecond = 1 / value
		}
		get() = 1 / framesPerSecond

	/** Be aware that changing frameDuration while animation is running will affect the current animation frame.
	 * Use [changeFrameDurationKeepingFrameIndex] instead */
	var framesPerSecond: Float = framesPerSecond
		set(value) {
			field = value
			updateTotalAnimationDuration()
		}

	val numberOfFrames: Int
		get() = frames.size

	/** Starts counting loops since the last reset. What a loop is depends on type of animation. *(So, for example, in a
	 * normalReversed animation, a loop is counted when the animation goes forward and backwards 1 time* **/
	var loopsFinished = 0

	private fun finishedOneLoop() {
		loopsFinished++

		if (loopsFinished == stopWhenFinishedThisNumberOfLoops) {
			pause()
			setToLastFrame()
		}
	}

	init {
		updateTotalAnimationDuration()
	}

	private fun updateTotalAnimationDuration() {
		var total = 0f
		for (frame in frames) {
			total += frameDuration * ((frame as? dev.dcostap.Assets2D.AnimFrameRegion)?.frameSpeedMultiplier ?: 1f)
		}
		this.totalAnimationDuration = total
	}

	fun update(delta: Float) {
		if (!isPaused) {
			finishedNormalAnimation = false
			finishedReversedAnimation = false
			when (animType) {
				AnimType.ONE_LOOP -> {
					elapsedTime += delta
					if (finishedNormalAnimation()) {
						setToLastFrame()
						finishedOneLoop()
						isPaused = true
					}
				}
				AnimType.LOOP -> {
					elapsedTime += delta
					if (finishedNormalAnimation()) {
						elapsedTime = 0f
						finishedOneLoop()
					}
				}
				AnimType.REVERSED -> {
					elapsedTime -= delta
					if (finishedReversedAnimation()) {
						setToLastFrame()
						elapsedTime += frameDuration - 0.0001f
						finishedOneLoop()
					}
				}
				AnimType.REVERSED_THEN_NORMAL -> {
					if (normalReversedCycle == AnimType.LOOP) {
						elapsedTime += delta
						if (finishedNormalAnimation()) {
							normalReversedCycle = AnimType.REVERSED
							elapsedTime = totalAnimationDuration - frameDuration - 0.0001f
						}
					} else if (normalReversedCycle == AnimType.REVERSED) {
						elapsedTime -= delta
						if (finishedReversedAnimation()) {
							normalReversedCycle = AnimType.LOOP
							elapsedTime = frameDuration
							finishedOneLoop()
						}
					}
				}
				AnimType.STOP_IN_EACH_NEW_FRAME -> {
					val previousFrame = currentFrame
					elapsedTime += delta
					val newFrame = currentFrame
					if (newFrame != previousFrame) {
						pause()
						currentFrame = newFrame
					}

					if (finishedNormalAnimation()) {
						finishedOneLoop()
					}
				}
			}
		}
	}

	fun getFrame(): T {
		return frameFromElapsedTime
	}

	/** Whether the animation finished playing forward
	 *
	 *
	 * More detailed: Returns true when the elapsed time is bigger than maximum elapsed time (depending on
	 * number of frames and frame duration)  */
	var finishedNormalAnimation: Boolean = false
		private set

	var finishedReversedAnimation: Boolean = false
		private set

	private fun finishedNormalAnimation(): Boolean {
		return (elapsedTime >= totalAnimationDuration).also { finishedNormalAnimation = it }
	}

	/** Whether the animation finished playing backwards
	 *
	 *
	 * More detailed: Returns true when the elapsed time is smaller than 0 */
	private fun finishedReversedAnimation(): Boolean {
		return (elapsedTime <= 0).also { finishedReversedAnimation = it }
	}

	private val frameFromElapsedTime: T
		get() {
			val index = currentFrame
			return frames[index]
		}

	fun durationForFrame(frame: Int) = customFrameDuration.get(frame, null) ?: frameDuration

	var currentFrame: Int = 0
		get() {
			if (frameDuration == 0f) return MathUtils.clamp(field, 0, frames.size - 1)

			var i = -1
			var time = 0f
			do {
				i++
				time += durationForFrame(i) * ((frames[i] as? dev.dcostap.Assets2D.AnimFrameRegion)?.frameSpeedMultiplier ?: 1f)
			} while (time < elapsedTime && i + 1 < frames.size)

			field = i
			return field
		}
		set(value) {
			var time = 0f
			var i = 0
			while (i < value) {
				time += durationForFrame(i) * ((frames[i] as? dev.dcostap.Assets2D.AnimFrameRegion)?.frameSpeedMultiplier ?: 1f)
				i++
			}
			time += 0.001f
			elapsedTime = time
			field = value
		}

	fun pause() {
		isPaused = true
	}

	fun resume() {
		isPaused = false
	}

	fun reset() {
		finishedNormalAnimation = false
		finishedReversedAnimation = false
		currentFrame = 0
		loopsFinished = 0
		isPaused = false
	}

	fun setToLastFrame() {
		currentFrame = frames.size - 1
	}
}
