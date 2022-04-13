package dev.dcostap

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import dev.dcostap.utils.map
import dev.dcostap.utils.newColorFrom255RGB
import java.io.File

object Shaders {
	/** Created by Darius on 24-Aug-19. */
	val defaultShader = SpriteBatch.createDefaultShader()

	var outlineShader: ShaderProgram = defaultShader
	var tintShader: ShaderProgram = defaultShader
	var colorShader: ShaderProgram = defaultShader
	var waterShader: ShaderProgram = defaultShader
	var shadowShader: ShaderProgram = defaultShader
	var replaceSkinColor: ShaderProgram = defaultShader
	var waterReplaceSkinColorAndTint: ShaderProgram = defaultShader
	var replaceSkinColorAndTint: ShaderProgram = defaultShader
	var motionBlurShader: ShaderProgram = defaultShader

	val tmpColor = Color()

	fun loadShaders() {
		outlineShader = Shader("outline", "outline")
		tintShader = Shader("default", "tint")
		colorShader = Shader("default", "color")
		waterShader = Shader("default", "water")
		shadowShader = Shader("default", "shadow")
		replaceSkinColor = Shader("default", "replaceSkinColor")
		waterReplaceSkinColorAndTint = Shader("default", "waterReplaceSkinColorAndTint")
		replaceSkinColorAndTint = Shader("default", "replaceSkinColorAndTint")
		motionBlurShader = Shader("motionBlur", "motionBlur")
	}

	fun disposeShaders() {
		outlineShader.dispose()
		tintShader.dispose()
		colorShader.dispose()
		waterShader.dispose()
		shadowShader.dispose()
		replaceSkinColor.dispose()
		waterReplaceSkinColorAndTint.dispose()
		motionBlurShader.dispose()
	}

	/** Using shaders, tints batch to the color specified. Color.BLACK means no tinting. */
	inline fun Batch.shaderTint(color: Color, f: () -> Unit) {
		flush()
		shader = tintShader
		tmpColor.set(color)
		tmpColor.a = 0f
		tintShader.setUniformf("u_emissive", tmpColor)
		f()
		flush()
		shader = null
	}

	/** @param originX 1f = rightmost, 0f = leftmost, 0.5f = center
	 * @param originY see [originX]
	 * @param blurSampleRadius 0.5 = size of non-blurred area is half of screen; 1 = non-blurred area is a tiny dot in the center
	 * @param blurSampleStrength 0 = none; 1 = very blurry */
	inline fun Batch.shaderMotionBlur(
		originX: Float,
		originY: Float,
		blurSampleRadius: Float,
		blurSampleStrength: Float,
		f: () -> Unit
	) {
		flush()
		shader = motionBlurShader
		shader.setUniformf("sampleDist", map(blurSampleRadius, 0f, 1f, 0f, 9f, Interpolation.pow2In))
		shader.setUniformf("sampleStrength", map(blurSampleStrength, 0f, 1f, 0f, 0.6f, Interpolation.pow2In))
		shader.setUniformf("originX", originX)
		shader.setUniformf("originY", originY)
		f()
		flush()
		shader = null
	}

	/** Replaces all non-transparent pixels with the color specified. */
	inline fun Batch.shaderColor(color: Color, f: () -> Unit) {
		flush()
		shader = colorShader
		tmpColor.set(color)
		colorShader.setUniformf("u_emissive", tmpColor)
		f()
		flush()
		shader = null
	}

	private class Shader(vert: String = "default", frag: String = "default") : ShaderProgram(
		Gdx.files.internal("shaders" + File.separator + vert + ".vert"),
		Gdx.files.internal("shaders" + File.separator + frag + ".frag")
	)

}