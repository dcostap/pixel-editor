package dev.dcostap

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Align
import dev.dcostap.utils.Utils
import dev.dcostap.utils.custom_sprite.CustomSprite
import dev.dcostap.utils.custom_sprite.CustomTextureRegion
import space.earlygrey.shapedrawer.ShapeDrawer

/**
 * Created by Darius on 14/09/2017.
 */
class GameDrawer(var batch: Batch) {
	private val DEFAULT_LINE_THICKNESS = 1.5f

	var alpha = 1f

	/** won't modify alpha */
	var color = Color(1f, 1f, 1f, 1f)
		set(value) {
			field.set(value)
		}

	/** won't modify alpha */
	fun setColor(r: Float, g: Float, b: Float) {
		color.r = r
		color.g = g
		color.b = b
	}

	fun setColor255(r: Int, g: Int, b: Int) {
		color.r = r / 255f
		color.g = g / 255f
		color.b = b / 255f
	}

	fun setColorAndAlpha(r: Float, g: Float, b: Float, a: Float) {
		color.r = r
		color.g = g
		color.b = b
		alpha = a
	}

	fun resetColor() {
		this.color.set(1f, 1f, 1f, 1f)
	}

	fun resetAlpha() {
		this.alpha = 1f
	}

	fun resetColorAndAlpha() {
		this.resetAlpha()
		this.resetColor()
	}

	private fun updateDrawingColorAndAlpha(color: Color) {
		batch.setColor(color.r, color.g, color.b, alpha)
	}

	private fun updateDrawingColorAndAlphaShapeDrawer(color: Color) {
		shapeDrawer!!.setColor(color.r, color.g, color.b, alpha)
	}

	private var dummyVector2 = Vector2()

	var drawingOffset = Vector2(0f, 0f)

	fun resetDrawingOffset() {
		drawingOffset.set(0f, 0f)
	}

	/** Local scaleX, used when no scaleX is specified on the draw methods */
	var scaleX = 1f

	/** Local scaleY, used when no scaleY is specified on the draw methods */
	var scaleY = 1f

	val origin = Vector2()

	/** Local rotation, used when no rotation is specified on the draw methods */
	var rotation = 0f

	/** Resets local scale and rotation to the default values */
	fun resetModifiers() {
		scaleX = 1f; scaleY = 1f
		rotation = 0f
		origin.set(0f, 0f)
		drawingOffset.setZero()
	}

	fun reset() {
		resetColorAndAlpha()
		resetModifiers()
	}

	private fun getFinalDrawingX(x: Float): Float {
		return x + drawingOffset.x
	}

	private fun getFinalDrawingY(y: Float): Float {
		return y + drawingOffset.y
	}

	/** returns the amount the texture needs to be scaled to be drawn according to the Pixels Per Meter (PPM) constant **/
	fun getWidth(textureRegion: TextureRegion) = textureRegion.regionWidth.toFloat()
	fun getHeight(textureRegion: TextureRegion) = textureRegion.regionHeight.toFloat()

	fun getWidth(texture: Texture) = texture.width.toFloat()
	fun getHeight(texture: Texture) = texture.height.toFloat()

	private val customSprite = dev.dcostap.utils.custom_sprite.CustomSprite()
	private val customTextureRegion = dev.dcostap.utils.custom_sprite.CustomTextureRegion()

	fun draw(
		textureRegion: TextureRegion,
		x: Float, y: Float,
		scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false,
		displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = false, centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false, centerOriginOnYAxis: Boolean = false,
		color: Color = this.color, alpha: Float = this.alpha,
		vertexTopLeft: Vector2,
		vertexTopRight: Vector2,
		vertexBottomLeft: Vector2,
		vertexBottomRight: Vector2,
	) {
		this.draw(
			textureRegion, x, y, scaleX, scaleY, rotation, originX, originY, mirrorX, mirrorY, displaceX, displaceY,
			customWidth, customHeight,
			centerOnXAxis, centerOnYAxis,
			centerOriginOnXAxis, centerOriginOnYAxis,
			color, alpha,
			vertexTopLeft.x, vertexTopLeft.y,
			vertexTopRight.x, vertexTopRight.y,
			vertexBottomLeft.x, vertexBottomLeft.y,
			vertexBottomRight.x, vertexBottomRight.y
		)
	}

	/**
	 * @param rotation in degrees.
	 *
	 * @param mirrorX flips the textureRegion, alternative to negative [scaleX].
	 * @param mirrorY flips the textureRegion, alternative to negative [scaleY].
	 *
	 * @param displaceX displaces the texture by its entire width. If 1 to the right, if -1 to the left, 0 disables it.
	 * Won't reset any previous displacement.
	 *
	 * @param displaceY see [displaceX]
	 *
	 * @param centerOriginOnXAxis sets the origin to the center of the texture.
	 * Note this only affects transformations (Scaling & rotation happen around the origin).
	 *
	 * @param centerOriginOnYAxis see [centerOriginOnXAxis]
	 *
	 * @param centerOnXAxis offsets the drawing by half of its width so the origin of the position X is in the middle of the texture, thus
	 * the texture is being drawn centered. Note that if true the previous displacement is temporarily reset
	 * @param centerOnYAxis see [centerOnXAxis]
	 */
	@JvmOverloads
	fun draw(
		textureRegion: TextureRegion,
		x: Float, y: Float,
		scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false,
		displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = false, centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false, centerOriginOnYAxis: Boolean = false,
		color: Color = this.color, alpha: Float = this.alpha,
		vertexTopLefX: Float = 0f, vertexTopLefY: Float = 0f,
		vertexTopRightX: Float = 0f, vertexTopRightY: Float = 0f,
		vertexBottomLefX: Float = 0f, vertexBottomLefY: Float = 0f,
		vertexBottomRightX: Float = 0f, vertexBottomRightY: Float = 0f,
	) {
		val oldAlpha = this.alpha
		this.alpha = alpha

		updateDrawingColorAndAlpha(color)

		customTextureRegion.texture = textureRegion.texture
		customTextureRegion.setRegion(
			textureRegion.u,
			textureRegion.v,
			textureRegion.u2,
			textureRegion.v2
		)

		customSprite.setRegion(customTextureRegion)

//		textureRegion.flip(mirrorX, mirrorY)
		val width = if (customWidth != -1f) customWidth else getWidth(textureRegion)
		val height = if (customHeight != -1f) customHeight else getHeight(textureRegion)

		val previousOffset = dummyVector2
		previousOffset.set(drawingOffset)

//        if (centerOnXAxis || centerOnYAxis) resetDrawingOffset()

		if (displaceX != 0 || displaceY != 0) {
			if (displaceX != 0) drawingOffset.x += (width * displaceX)
			if (displaceY != 0) drawingOffset.y += (height * displaceY)
		}

		if (centerOnXAxis) drawingOffset.x += -width / 2f
		if (centerOnYAxis) drawingOffset.y += -height / 2f

		val thisX = getFinalDrawingX(x)
		val thisY = getFinalDrawingY(y)

		customSprite.rotation = rotation
		customSprite.setScale(scaleX, scaleY)
		customSprite.setOrigin(
			if (!centerOriginOnXAxis) originX else (width / 2f),
			if (!centerOriginOnYAxis) originY else (height / 2f)
		)
		customSprite.setFlip(mirrorX, mirrorY)
		customSprite.setBounds(thisX, thisY, width, height)
		customSprite.setAlpha(alpha)

		customSprite.vertexTopLefX = vertexTopLefX
		customSprite.vertexTopLefY = vertexTopLefY
		customSprite.vertexTopRightX = vertexTopRightX
		customSprite.vertexTopRightY = vertexTopRightY
		customSprite.vertexBottomLefX = vertexBottomLefX
		customSprite.vertexBottomLefY = vertexBottomLefY
		customSprite.vertexBottomRightX = vertexBottomRightX
		customSprite.vertexBottomRightY = vertexBottomRightY

		// count = 20: hardcoded since SPRITE_SIZE inside Sprite.java isn't public
		//		static final int VERTEX_SIZE = 2 + 1 + 2;
		//		static final int SPRITE_SIZE = 4 * VERTEX_SIZE;
		batch.draw(customSprite.texture, customSprite.vertices, 0, 20)

		drawingOffset.set(previousOffset)
		textureRegion.flip(mirrorX, mirrorY)
		this.alpha = oldAlpha
	}

	@JvmOverloads
	fun draw(
		ninePatch: NinePatch,
		x: Float, y: Float,
		width: Float, height: Float,
		scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		centerOnXAxis: Boolean = false, centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false, centerOriginOnYAxis: Boolean = false,
		color: Color = this.color, alpha: Float = this.alpha
	) {
		val oldAlpha = this.alpha
		this.alpha = alpha
		updateDrawingColorAndAlpha(color)

		val previousOffset = dummyVector2
		previousOffset.set(drawingOffset)

		if (centerOnXAxis || centerOnYAxis) resetDrawingOffset()

		if (centerOnXAxis) drawingOffset.x += -width / 2f
		if (centerOnYAxis) drawingOffset.y += -height / 2f

		val thisX = getFinalDrawingX(x)
		val thisY = getFinalDrawingY(y)

		ninePatch.draw(
			batch, thisX, thisY, if (!centerOriginOnXAxis) originX else (width / 2f),
			if (!centerOriginOnYAxis) originY else (height / 2f), width, height, scaleX, scaleY, rotation
		)

		drawingOffset.set(previousOffset)
		this.alpha = oldAlpha
	}

	@JvmOverloads
	fun draw(
		texture: Texture, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = false, centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false, centerOriginOnYAxis: Boolean = false,
		srcX: Int = -1, srcY: Int = -1, srcWidth: Int = -1, srcHeight: Int = -1,
		color: Color = this.color, alpha: Float = this.alpha
	) {
		val oldAlpha = this.alpha
		this.alpha = alpha
		updateDrawingColorAndAlpha(color)
		val width = if (customWidth != -1f) customWidth else getWidth(texture)
		val height = if (customHeight != -1f) customHeight else getHeight(texture)

		val previousOffset = dummyVector2
		previousOffset.set(drawingOffset)

		if (centerOnXAxis || centerOnYAxis) resetDrawingOffset()

		if (displaceX != 0 || displaceY != 0) {
			if (displaceX != 0) drawingOffset.x += (width * displaceX)
			if (displaceY != 0) drawingOffset.y += (height * displaceY)
		}

		if (centerOnXAxis) drawingOffset.x += -width / 2f
		if (centerOnYAxis) drawingOffset.y += -height / 2f

		val thisX = getFinalDrawingX(x)
		val thisY = getFinalDrawingY(y)

		if (srcX <= 0 && srcY <= 0 && srcHeight <= 0 && srcWidth <= 0) {
			batch.draw(
				texture, thisX, thisY, if (!centerOriginOnXAxis) originX else (width / 2f),
				if (!centerOriginOnYAxis) originY else (height / 2f), width, height, scaleX, scaleY, rotation,
				0, 0, texture.width, texture.height, mirrorX, mirrorY
			)
		} else {
			batch.draw(
				texture, thisX, thisY, if (!centerOriginOnXAxis) originX else (width / 2f),
				if (!centerOriginOnYAxis) originY else (height / 2f), width, height, scaleX, scaleY, rotation,
				srcX, srcY,
				Math.min(srcWidth, texture.width),
				Math.min(srcHeight, texture.height), false, false
			)
		}

		drawingOffset.set(previousOffset)
		this.alpha = oldAlpha
	}

	@JvmOverloads
	fun draw(
		textureName: String, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = false, centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false, centerOriginOnYAxis: Boolean = false,
		color: Color = this.color, alpha: Float = this.alpha
	) {
		this.draw(
			dev.dcostap.Assets2D.getRegion(textureName), x, y, scaleX, scaleY, rotation, originX, originY, mirrorX, mirrorY,
			displaceX, displaceY, customWidth, customHeight, centerOnXAxis, centerOnYAxis,
			centerOriginOnXAxis, centerOriginOnYAxis, color, alpha
		)
	}

	@JvmOverloads
	fun draw(
		textureRegion: TextureRegion,
		position: Vector2,
		scaleX: Float = this.scaleX,
		scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x,
		originY: Float = this.origin.y,
		mirrorX: Boolean = false,
		mirrorY: Boolean = false,
		displaceX: Int = 0,
		displaceY: Int = 0,
		customWidth: Float = -1f,
		customHeight: Float = -1f,
		centerOnXAxis: Boolean = false,
		centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false,
		centerOriginOnYAxis: Boolean = false,
		color: Color = this.color,
		alpha: Float = this.alpha
	) {
		this.draw(
			textureRegion,
			position.x,
			position.y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOnXAxis,
			centerOnYAxis,
			centerOriginOnXAxis,
			centerOriginOnYAxis,
			color = color,
			alpha = alpha
		)
	}

	@JvmOverloads
	fun draw(
		textureName: String, position: Vector2, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = false, centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false, centerOriginOnYAxis: Boolean = false, color: Color = this.color
	) {
		this.draw(
			dev.dcostap.Assets2D.getRegion(textureName),
			position.x,
			position.y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOnXAxis,
			centerOnYAxis,
			centerOriginOnXAxis,
			centerOriginOnYAxis,
			color = color
		)
	}

	@JvmOverloads
	fun drawCentered(
		textureName: String, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = true, centerOnYAxis: Boolean = true, color: Color = this.color
	) {
		this.draw(
			dev.dcostap.Assets2D.getRegion(textureName),
			x,
			y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOnXAxis,
			centerOnYAxis,
			color = color
		)
	}

	@JvmOverloads
	fun drawCentered(
		textureRegion: TextureRegion, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = true, centerOnYAxis: Boolean = true, color: Color = this.color
	) {
		this.draw(
			textureRegion,
			x,
			y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOnXAxis,
			centerOnYAxis,
			color = color
		)
	}

	@JvmOverloads
	fun drawWithOriginOnCenter(
		textureRegion: TextureRegion, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation, originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOriginOnXAxis: Boolean = true, centerOriginOnYAxis: Boolean = true, color: Color = this.color
	) {
		this.draw(
			textureRegion,
			x,
			y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOriginOnXAxis = centerOriginOnXAxis,
			centerOriginOnYAxis = centerOriginOnYAxis,
			color = color
		)
	}

	@JvmOverloads
	fun drawWithOriginOnCenter(
		textureRegion: TextureRegion, position: Vector2, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation, originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOriginOnXAxis: Boolean = true, centerOriginOnYAxis: Boolean = true, color: Color = this.color
	) {
		this.draw(
			textureRegion,
			position.x,
			position.y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOriginOnXAxis = centerOriginOnXAxis,
			centerOriginOnYAxis = centerOriginOnYAxis,
			color = color
		)
	}

	@JvmOverloads
	fun drawWithOriginOnCenter(
		textureName: String, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation, originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOriginOnXAxis: Boolean = true, centerOriginOnYAxis: Boolean = true, color: Color = this.color
	) {
		this.draw(
			dev.dcostap.Assets2D.getRegion(textureName),
			x,
			y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOriginOnXAxis = centerOriginOnXAxis,
			centerOriginOnYAxis = centerOriginOnYAxis,
			color = color
		)
	}

	@JvmOverloads
	fun drawCenteredWithOriginOnCenter(
		textureRegion: TextureRegion, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation, originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = true, centerOnYAxis: Boolean = true,
		centerOriginOnXAxis: Boolean = true, centerOriginOnYAxis: Boolean = true, color: Color = this.color
	) {
		this.draw(
			textureRegion,
			x,
			y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOnXAxis,
			centerOnYAxis,
			centerOriginOnXAxis,
			centerOriginOnYAxis,
			color = color
		)
	}

	@JvmOverloads
	fun drawCenteredWithOriginOnCenter(
		textureName: String, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation, originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = true, centerOnYAxis: Boolean = true,
		centerOriginOnXAxis: Boolean = true, centerOriginOnYAxis: Boolean = true,
		color: Color = this.color, alpha: Float = this.alpha
	) {
		this.draw(
			dev.dcostap.Assets2D.getRegion(textureName),
			x,
			y,
			scaleX,
			scaleY,
			rotation,
			originX,
			originY,
			mirrorX,
			mirrorY,
			displaceX,
			displaceY,
			customWidth,
			customHeight,
			centerOnXAxis,
			centerOnYAxis,
			centerOriginOnXAxis,
			centerOriginOnYAxis,
			color = color,
			alpha = alpha
		)
	}

	private val tmpCharRegion = TextureRegion()

	@JvmOverloads
	fun drawChar(
		char: Char, font: BitmapFont, x: Float, y: Float, scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		mirrorX: Boolean = false, mirrorY: Boolean = false, displaceX: Int = 0, displaceY: Int = 0,
		customWidth: Float = -1f, customHeight: Float = -1f,
		centerOnXAxis: Boolean = false, centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false, centerOriginOnYAxis: Boolean = false,
		color: Color = this.color
	) {
		val glyph = font.data.getGlyph(char)
		tmpCharRegion.texture = font.regions[glyph.page].texture
		tmpCharRegion.setRegion(glyph.u, glyph.v, glyph.u2, glyph.v2)
		tmpCharRegion.flip(false, true)
		this.draw(
			tmpCharRegion, x, y, scaleX, scaleY, rotation, originX, originY, mirrorX, mirrorY,
			displaceX, displaceY, customWidth, customHeight, centerOnXAxis, centerOnYAxis,
			centerOriginOnXAxis, centerOriginOnYAxis, color = color
		)
	}

	private val fontColor = Color()

	@JvmOverloads
	fun drawText(
		text: Any?, x: Float, y: Float, font: BitmapFont, color: Color = this.color,
		scaleX: Float = this.scaleX, scaleY: Float = this.scaleY,
		hAlign: Int = Align.left, targetWidth: Float = 0f, wrap: Boolean = false
	) {
		val endScaleX = scaleX
		val endScaleY = scaleY
		if (endScaleX == 0f || endScaleY == 0f) return

		val oldColor = font.color
		fontColor.set(color)
		fontColor.a = alpha

		font.color = fontColor

		val oldScaleX = font.scaleX
		val oldScaleY = font.scaleY
		val usedIntegers = font.usesIntegerPositions()
		font.setUseIntegerPositions(false)
		font.data.setScale(endScaleX, endScaleY)

		font.draw(batch, text.toString(), x + drawingOffset.x, y + drawingOffset.y, targetWidth, hAlign, wrap)

		font.data.setScale(oldScaleX, oldScaleY)
		font.setUseIntegerPositions(usedIntegers)
		font.color = oldColor
	}

	/**
	 * Draws a Rectangle using "pixel" image on atlas
	 *
	 * @param x         bottom-left corner x
	 * @param y         bottom-left corner y
	 * @param thickness size of the borders if the rectangle is not filled
	 * @param fill      whether the rectangle drawn will be filled with the color
	 */
	@JvmOverloads
	fun drawRectangle(
		x: Float,
		y: Float,
		width: Float,
		height: Float,
		fill: Boolean,
		thickness: Float = DEFAULT_LINE_THICKNESS,
		originX: Float = this.origin.x,
		originY: Float = this.origin.y,
		scaleX: Float = this.scaleX,
		scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		color: Color = this.color
	) {
		updateDrawingColorAndAlpha(color)
		val thisX = getFinalDrawingX(x)
		val thisY = getFinalDrawingY(y)

		if (!fill) {
			batch.draw(
				dev.dcostap.Assets2D.pixel,
				thisX + thickness,
				thisY,
				originX,
				originY,
				width - thickness * 2,
				thickness,
				scaleX,
				scaleY,
				rotation
			)
			batch.draw(dev.dcostap.Assets2D.pixel, thisX, thisY, originX, originY, thickness, height, scaleX, scaleY, rotation)
			batch.draw(
				dev.dcostap.Assets2D.pixel,
				thisX + thickness,
				thisY + height - thickness,
				originX,
				originY,
				width - thickness * 2,
				thickness,
				scaleX,
				scaleY,
				rotation
			)
			batch.draw(
				dev.dcostap.Assets2D.pixel,
				thisX + width - thickness,
				thisY,
				originX,
				originY,
				thickness,
				height,
				scaleX,
				scaleY,
				rotation
			)
		} else {
			batch.draw(dev.dcostap.Assets2D.pixel, thisX, thisY, originX, originY, width, height, scaleX, scaleY, rotation)
		}
	}

	private var shapeDrawer: ShapeDrawer? = null
		get() {
			if (field == null) field = ShapeDrawer(batch, dev.dcostap.Assets2D.pixel)
			field!!.pixelSize = 1f//pixelSizeScreen())
			return field
		}

	@JvmOverloads
	fun drawCircle(
		x: Float,
		y: Float,
		radius: Float,
		fill: Boolean = true,
		lineThickness: Float = DEFAULT_LINE_THICKNESS,
		scale: Float = 1f,
		color: Color = this.color
	) {
		updateDrawingColorAndAlphaShapeDrawer(color)
		val thisX = getFinalDrawingX(x)
		val thisY = getFinalDrawingY(y)

//        batch.draw(pixelCircle, thisX, thisY, originX, originY, size, size, scaleX, scaleY, rotation)

		if (!fill)
			shapeDrawer!!.circle(thisX, thisY, radius * scale, lineThickness * scale)
		else
			shapeDrawer!!.filledCircle(thisX, thisY, radius * scale)
	}

	@JvmOverloads
	fun drawArc(
		x: Float, y: Float, radius: Float, startAngle: Float, angleAmount: Float,
		fill: Boolean = true, lineThickness: Float = DEFAULT_LINE_THICKNESS,
		scale: Float = 1f, color: Color = this.color
	) {
		updateDrawingColorAndAlphaShapeDrawer(color)
		val thisX = getFinalDrawingX(x)
		val thisY = getFinalDrawingY(y)

		if (!fill)
			shapeDrawer!!.arc(
				thisX, thisY, radius * scale,
				MathUtils.degreesToRadians * startAngle,
				MathUtils.degreesToRadians * angleAmount, lineThickness * scale
			)
		else
			shapeDrawer!!.sector(
				thisX, thisY, radius * scale,
				MathUtils.degreesToRadians * startAngle,
				MathUtils.degreesToRadians * angleAmount
			)
	}

	@JvmOverloads
	fun drawEllipse(
		x: Float, y: Float, radiusX: Float, radiusY: Float,
		fill: Boolean = true, lineThickness: Float = DEFAULT_LINE_THICKNESS,
		rotation: Float = 0f, scale: Float = 1f, color: Color = this.color
	) {
		updateDrawingColorAndAlphaShapeDrawer(color)
		val thisX = getFinalDrawingX(x)
		val thisY = getFinalDrawingY(y)

//        batch.draw(pixelCircle, thisX, thisY, originX, originY, size, size, scaleX, scaleY, rotation)

		if (!fill)
			shapeDrawer!!.ellipse(thisX, thisY, radiusX * scale, radiusY * scale, rotation, lineThickness * scale)
		else
			shapeDrawer!!.filledEllipse(thisX, thisY, radiusX * scale, radiusY * scale, rotation)
	}

	@JvmOverloads
	fun drawPolygon(
		polygon: Polygon, fill: Boolean = true, lineThickness: Float = DEFAULT_LINE_THICKNESS,
		color: Color = this.color
	) {
		updateDrawingColorAndAlphaShapeDrawer(color)

		var even = true
		val verts = polygon.transformedVertices
		verts.forEachIndexed { index, fl ->
			verts[index] =
				if (even) fl + drawingOffset.x
				else fl + drawingOffset.y
			even = !even
		}

		if (!fill)
			shapeDrawer!!.polygon(polygon, lineThickness)
		else
			shapeDrawer!!.filledPolygon(polygon)

		even = true
		verts.forEachIndexed { index, fl ->
			verts[index] =
				if (even) fl - drawingOffset.x
				else fl - drawingOffset.y
			even = !even
		}
	}

	@JvmOverloads
	fun drawCircle(
		circle: Circle, fill: Boolean = true, lineThickness: Float = DEFAULT_LINE_THICKNESS,
		scale: Float = 1f, color: Color = this.color
	) {
		this.drawCircle(circle.x, circle.y, circle.radius, fill, lineThickness, scale, color)
	}

	fun drawRectangle(
		rectangle: Rectangle, fill: Boolean, thickness: Float = DEFAULT_LINE_THICKNESS,
		originX: Float = this.origin.x, originY: Float = this.origin.y,
		scaleX: Float = this.scaleX, scaleY: Float = this.scaleY, rotation: Float = this.rotation,
		color: Color = this.color
	) {
		this.drawRectangle(
			rectangle.x, rectangle.y, rectangle.width, rectangle.height, fill, thickness,
			originX, originY, scaleX, scaleY, rotation, color
		)
	}

	fun drawShape(
		shape2D: Shape2D,
		thickness: Float = DEFAULT_LINE_THICKNESS,
		fill: Boolean = false,
		originX: Float = this.origin.x,
		originY: Float = this.origin.y,
		scaleX: Float = this.scaleX,
		scaleY: Float = this.scaleY,
		rotation: Float = this.rotation,
		color: Color = this.color
	) {
		if (shape2D is Rectangle)
			this.drawRectangle(shape2D, fill, thickness, originX, originY, scaleX, scaleY, rotation, color)
		else if (shape2D is Circle)
			this.drawCircle(shape2D, fill, thickness, Math.max(scaleX, scaleY), color)
		else if (shape2D is Polygon) {
			drawPolygon(shape2D, fill, thickness, color)
		}
	}

	fun drawCross(
		x: Float,
		y: Float,
		axisSize: Float,
		thickness: Float = DEFAULT_LINE_THICKNESS,
		color: Color = this.color
	) {
		this.drawLine(x - axisSize, y, x + axisSize, y, thickness, color = color)
		this.drawLine(x, y - axisSize, x, y + axisSize, thickness, color = color)
	}

	/**
	 * Draws a line using "pixel" image on atlas. Lines drawn together will not be correctly joined.
	 *
	 * @param x1        start x
	 * @param y1        start y
	 * @param x2        end x
	 * @param y2        end y
	 * @param thickness size of the line
	 */
	@JvmOverloads
	fun drawLine(
		x1: Float,
		y1: Float,
		x2: Float,
		y2: Float,
		thickness: Float = DEFAULT_LINE_THICKNESS,
		isArrow: Boolean = false,
		arrowSize: Float = DEFAULT_LINE_THICKNESS * 10,
		color: Color = this.color
	) {
		updateDrawingColorAndAlpha(color)
		val thisX1 = getFinalDrawingX(x1)
		val thisX2 = getFinalDrawingX(x2)
		val thisY1 = getFinalDrawingY(y1)
		val thisY2 = getFinalDrawingY(y2)

		val dx = thisX2 - thisX1
		val dy = thisY2 - thisY1
		val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
		val deg = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
		batch.draw(dev.dcostap.Assets2D.pixel, thisX1, thisY1, 0f, thickness / 2f, dist, thickness, 1f, 1f, deg)

		if (isArrow) {
			val angle = Math.toRadians(Utils.angleBetween(x1, y1, x2, y2).toDouble())
			val angleDiff = -40
			val angle1 = angle + angleDiff
			val angle2 = angle - angleDiff
			drawLine(
				x2,
				y2,
				(x2 + (Math.cos(angle1) * arrowSize)).toFloat(),
				(y2 + (Math.sin(angle1) * arrowSize)).toFloat(),
				thickness,
				isArrow = false
			)
			drawLine(
				x2,
				y2,
				(x2 + (Math.cos(angle2) * arrowSize)).toFloat(),
				(y2 + (Math.sin(angle2) * arrowSize)).toFloat(),
				thickness,
				isArrow = false
			)
		}
	}

	fun drawLine(
		start: Vector2, end: Vector2, thickness: Float = DEFAULT_LINE_THICKNESS,
		isArrow: Boolean = false, arrowSize: Float = DEFAULT_LINE_THICKNESS * 10, color: Color = this.color
	) {
		this.drawLine(start.x, start.y, end.x, end.y, thickness, isArrow, arrowSize, color)
	}

	fun drawLineFromAngle(
		x1: Float, y1: Float, distance: Float, angleDegrees: Float, thickness: Float = DEFAULT_LINE_THICKNESS,
		isArrow: Boolean = false, arrowSize: Float = DEFAULT_LINE_THICKNESS * 10, color: Color = this.color
	) {
		this.drawLine(
			x1,
			y1,
			((x1 + distance * Math.cos(Math.toRadians(angleDegrees.toDouble()))).toFloat()),
			((y1 + distance * Math.sin(Math.toRadians(angleDegrees.toDouble()))).toFloat()),
			thickness,
			isArrow,
			arrowSize,
			color
		)
	}
}