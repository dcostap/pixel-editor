package dev.dcostap

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Align
import space.earlygrey.shapedrawer.ShapeDrawer

interface Transformable {
	var x: Float
	var y: Float
	val position: Vector2
	val scale: Vector2
	val origin: Vector2
	var rotation: Float

	fun setTransform(transform: Transformable)
	fun addTransform(transform: Transformable)
}

class Transform : Transformable {
	override val position = Vector2()
	override val scale = Vector2(1f, 1f)
	override val origin = Vector2()
	override var rotation = 0f

	override var x
		get() = position.x
		set(value) {
			position.x = value
		}

	override var y
		get() = position.y
		set(value) {
			position.y = value
		}

	override fun setTransform(transform: Transformable) {
		this.scale.set(transform.scale)
		this.position.set(transform.position)
		this.origin.set(transform.origin)
		this.rotation = transform.rotation
	}

	override fun addTransform(transform: Transformable) {
		this.scale.scl(transform.scale)
		this.position.add(transform.position)
		this.origin.add(transform.origin)
		this.rotation += transform.rotation
	}
}

/**
 * Created by Darius on 14/09/2017.
 */
class Drawer2D(var batch: Batch) : Transformable by Transform() {
	private val DEFAULT_LINE_THICKNESS = 1.5f

	var alpha = 1f

	val tmpTransform = Transform()

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
		shapeDrawer.setColor(color.r, color.g, color.b, alpha)
	}

	private var dummyVector2 = Vector2()

	/** Resets local scale and rotation to the default values */
	fun reset() {
		scale.set(1f, 1f)
		rotation = 0f
		origin.setZero()
		position.setZero()
		resetColorAndAlpha()
	}

	/** returns the amount the texture needs to be scaled to be drawn according to the Pixels Per Meter (PPM) constant **/
	fun getWidth(textureRegion: TextureRegion) = textureRegion.regionWidth.toFloat()
	fun getHeight(textureRegion: TextureRegion) = textureRegion.regionHeight.toFloat()

	fun getWidth(texture: Texture) = texture.width.toFloat()
	fun getHeight(texture: Texture) = texture.height.toFloat()

	private val customSprite = dev.dcostap.utils.custom_sprite.CustomSprite()
	private val customTextureRegion = dev.dcostap.utils.custom_sprite.CustomTextureRegion()

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
	fun draw(
		textureRegion: TextureRegion,
		x: Float = 0f, y: Float = 0f,
		scaleX: Float = 1f, scaleY: Float = 1f,
		rotation: Float = 0f,
		originX: Float = 0f, originY: Float = 0f,
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

		val width = if (customWidth != -1f) customWidth else getWidth(textureRegion)
		val height = if (customHeight != -1f) customHeight else getHeight(textureRegion)

		tmpTransform.setTransform(this)

		if (displaceX != 0 || displaceY != 0) {
			if (displaceX != 0) tmpTransform.position.x += (width * displaceX)
			if (displaceY != 0) tmpTransform.position.y += (height * displaceY)
		}

		if (centerOnXAxis) tmpTransform.x += -width / 2f
		if (centerOnYAxis) tmpTransform.y += -height / 2f

		tmpTransform.x += x
		tmpTransform.y += y

		tmpTransform.scale.scl(scaleX, scaleY)
		tmpTransform.rotation += rotation

		tmpTransform.origin.add(originX, originY)

		customSprite.rotation = tmpTransform.rotation
		customSprite.setScale(tmpTransform.scale.x, tmpTransform.scale.y)
		customSprite.setOrigin(
			if (!centerOriginOnXAxis) tmpTransform.origin.x else (width / 2f),
			if (!centerOriginOnYAxis) tmpTransform.origin.y else (height / 2f)
		)
		customSprite.setFlip(mirrorX, mirrorY)
		customSprite.setBounds(tmpTransform.x, tmpTransform.y, width, height)
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

		textureRegion.flip(mirrorX, mirrorY)
		this.alpha = oldAlpha
	}

	fun draw(
		ninePatch: NinePatch,
		x: Float = 0f, y: Float = 0f,
		width: Float, height: Float,
		scaleX: Float = 1f, scaleY: Float = 1f,
		rotation: Float = 0f,
		originX: Float = 0f, originY: Float = 0f,
		centerOnXAxis: Boolean = false, centerOnYAxis: Boolean = false,
		centerOriginOnXAxis: Boolean = false, centerOriginOnYAxis: Boolean = false,
		color: Color = this.color, alpha: Float = this.alpha
	) {
		val oldAlpha = this.alpha
		this.alpha = alpha

		updateDrawingColorAndAlpha(color)

		tmpTransform.setTransform(this)

		if (centerOnXAxis) tmpTransform.x += -width / 2f
		if (centerOnYAxis) tmpTransform.y += -height / 2f

		tmpTransform.x += x
		tmpTransform.y += y

		tmpTransform.scale.scl(scaleX, scaleY)
		tmpTransform.rotation += rotation

		tmpTransform.origin.add(originX, originY)

		ninePatch.draw(
			batch, tmpTransform.x, tmpTransform.y, if (!centerOriginOnXAxis) originX else (width / 2f),
			if (!centerOriginOnYAxis) originY else (height / 2f), width, height, scaleX, scaleY, rotation
		)

		this.alpha = oldAlpha
	}

	private val tmpCharRegion = TextureRegion()

	fun drawChar(
		char: Char, font: BitmapFont,
		x: Float = 0f, y: Float = 0f,
		scaleX: Float = 1f, scaleY: Float = 1f,
		rotation: Float = 0f,
		originX: Float = 0f, originY: Float = 0f,
		mirrorX: Boolean = false, mirrorY: Boolean = false,
		displaceX: Int = 0, displaceY: Int = 0,
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

	fun drawText(
		text: Any?,
		font: BitmapFont,
		x: Float = 0f, y: Float = 0f,
		color: Color = this.color,
		scaleX: Float = 1f, scaleY: Float = 1f,
		hAlign: Int = Align.left, targetWidth: Float = 0f, wrap: Boolean = false
	) {
		val endScaleX = scaleX * this.scale.x
		val endScaleY = scaleY * this.scale.y
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

		font.draw(batch, text.toString(), x + this.x, y + this.y, targetWidth, hAlign, wrap)

		font.data.setScale(oldScaleX, oldScaleY)
		font.setUseIntegerPositions(usedIntegers)
		font.color = oldColor
	}

	private val shapeDrawer: ShapeDrawer by lazy {
		ShapeDrawer(batch, Assets2D.pixel).also { it.pixelSize = 1f }
	}

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
		val thisX = x + position.x
		val thisY = y + position.y

		if (!fill)
			shapeDrawer.circle(thisX, thisY, radius * scale, lineThickness * scale)
		else
			shapeDrawer.filledCircle(thisX, thisY, radius * scale)
	}

	fun drawArc(
		x: Float, y: Float, radius: Float, startAngle: Float, angleAmount: Float,
		fill: Boolean = true, lineThickness: Float = DEFAULT_LINE_THICKNESS,
		scale: Float = 1f, color: Color = this.color
	) {
		updateDrawingColorAndAlphaShapeDrawer(color)
		val thisX = x + position.x
		val thisY = y + position.y

		if (!fill)
			shapeDrawer.arc(
				thisX, thisY, radius * scale,
				MathUtils.degreesToRadians * startAngle,
				MathUtils.degreesToRadians * angleAmount, lineThickness * scale
			)
		else
			shapeDrawer.sector(
				thisX, thisY, radius * scale,
				MathUtils.degreesToRadians * startAngle,
				MathUtils.degreesToRadians * angleAmount
			)
	}

	fun drawEllipse(
		x: Float, y: Float, radiusX: Float, radiusY: Float,
		fill: Boolean = true, lineThickness: Float = DEFAULT_LINE_THICKNESS,
		rotation: Float = 0f, scale: Float = 1f, color: Color = this.color
	) {
		updateDrawingColorAndAlphaShapeDrawer(color)
		val thisX = x + position.x
		val thisY = y + position.y

		if (!fill)
			shapeDrawer.ellipse(
				thisX,
				thisY,
				radiusX * scale,
				radiusY * scale,
				rotation * this.rotation,
				lineThickness * scale
			)
		else
			shapeDrawer.filledEllipse(thisX, thisY, radiusX * scale, radiusY * scale, rotation * this.rotation)
	}

	fun drawRectangle(
		x: Float = 0f, y: Float = 0f, width: Float, height: Float,
		fill: Boolean = true,
		lineThickness: Float = DEFAULT_LINE_THICKNESS,
		color: Color = this.color
	) {
		updateDrawingColorAndAlphaShapeDrawer(color)

		if (!fill)
			shapeDrawer.rectangle(
				x + this.x,
				y + this.y,
				width * this.scale.x,
				height * this.scale.y,
				color,
				lineThickness
			)
		else
			shapeDrawer.filledRectangle(x + this.x, y + this.y, width * this.scale.x, height * this.scale.y, color)
	}

	fun drawPolygon(
		polygon: Polygon, fill: Boolean = true, lineThickness: Float = DEFAULT_LINE_THICKNESS,
		color: Color = this.color
	) {
		updateDrawingColorAndAlphaShapeDrawer(color)

		var even = true
		val verts = polygon.transformedVertices
		verts.forEachIndexed { index, fl ->
			verts[index] =
				if (even) fl + position.x
				else fl + position.y
			even = !even
		}

		if (!fill)
			shapeDrawer.polygon(polygon, lineThickness)
		else
			shapeDrawer.filledPolygon(polygon)

		even = true
		verts.forEachIndexed { index, fl ->
			verts[index] =
				if (even) fl - position.x
				else fl - position.y
			even = !even
		}
	}

	fun drawCircle(
		circle: Circle, fill: Boolean = true, lineThickness: Float = DEFAULT_LINE_THICKNESS,
		scale: Float = 1f, color: Color = this.color
	) {
		this.drawCircle(circle.x, circle.y, circle.radius, fill, lineThickness, scale, color)
	}
}