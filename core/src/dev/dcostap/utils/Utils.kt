@file:JvmName("GlobalUtils")
@file:JvmMultifileClass

package dev.dcostap.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.*
import com.badlogic.gdx.scenes.scene2d.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Cell
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.viewport.Viewport
import dev.dcostap.utils.ui.ExtLabel
import dev.dcostap.utils.ui.ExtTable
import dev.dcostap.Debug.logWarning
import com.kotcrab.vis.ui.widget.CollapsibleWidget
import com.kotcrab.vis.ui.widget.VisCheckBox
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisSlider
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.SimpleFloatSpinnerModel
import com.kotcrab.vis.ui.widget.spinner.Spinner
import ktx.actors.onChange
import ktx.collections.*
import org.lwjgl.glfw.GLFW
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import kotlin.math.*
import kotlin.reflect.KClass

typealias Easing = Interpolation

/** Substitute of kotlin's .let; call after a variable to execute block of code (only when the variable isn't null)
 * with local non-nullable copy of the variable: this avoids the "variable could have been modified" complaint from the compiler
 *
 * Just like with .let, inside of the block reference reference the variable with **it**
 *
 * Equivalent using .let: **variable?.let {}**; with this: **variable.ifNotNull {}** */
inline fun <T : Any?> T?.ifNotNull(f: (it: T) -> Unit): T? {
	return if (this != null) {
		f(this); this
	} else null
}

/** Automatically frees object when finished */
inline fun <B : Any> pool(clazz: KClass<B>, action: (B) -> Unit) {
	val value = Pools.obtain(clazz.java)
	action(value)
	Pools.free(value)
}

fun <T> Class<T>.newInstance(): T {
	return this.getDeclaredConstructor().newInstance()
}

inline fun <T1 : Any, T2 : Any> ifNotNull(p1: T1?, p2: T2?, block: (T1, T2) -> Unit): Unit? {
	return if (p1 != null && p2 != null) block(p1, p2) else null
}

inline fun <T1 : Any, T2 : Any, T3 : Any> ifNotNull(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> Unit): Unit? {
	return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

inline fun <T : Any> T?.ifNull(f: () -> Unit): T? {
	return if (this == null) {
		f(); this
	} else this
}

operator fun <Type> GdxArray<Type>.plusAssign(elements: Iterable<Type>) {
	this.addAll(elements)
}

operator fun <Type> GdxArray<Type>.plusAssign(element: Type) {
	this.add(element)
}

operator fun <Type> GdxArray<Type>.plusAssign(elements: Array<out Type>) {
	this.addAll(elements, 0, elements.size)
}

fun newRectangleFromRegion(region: TextureRegion, centerX: Boolean = false, centerY: Boolean = false): Rectangle {
	return newRectangle(
		if (centerX) -region.regionWidth / 2f else 0f, if (centerY) -region.regionHeight / 2f else 0f,
		region.regionWidth, region.regionHeight
	)
}

fun newPolygon(vertices: Array<Pair<Float, Float>>, centerX: Boolean = false, centerY: Boolean = false): Polygon {
	val vertices = vertices.map { Pair(it.first.toFloat(), it.second.toFloat()) }
	return Polygon(floatArrayOf(*vertices.flatMap { it.toList() }.toFloatArray())).also {
		if (centerX || centerY) {
			var minX = Float.MAX_VALUE
			var maxX = Float.MIN_VALUE

			var minY = Float.MAX_VALUE
			var maxY = Float.MIN_VALUE

			var even = true
			it.vertices.forEachIndexed { i, p ->
				if (even)
					when {
						p < minX -> minX = p
						p > maxX -> maxX = p
					}
				else
					when {
						p < minY -> minY = p
						p > maxY -> maxY = p
					}

				even = !even
			}

			val thisWidth = maxX - minX
			val thisHeight = maxY - minY

			it.setPosition(if (centerX) -thisWidth / 2f else 0f, if (centerY) -thisHeight / 2f else 0f)
		}
	}
}

fun Vector2.distanceBetween(vector: Vector2): Float {
	return this.dst(vector)
}

fun Vector2.distanceBetween(x: Float, y: Float): Float {
	return this.dst(x, y)
}

fun Vector2.addAngleMovement(distance: Float, directionDegrees: Float, delta: Float = 1f): Vector2 {
	x += Utils.angleMovementX(distance, directionDegrees) * delta
	y += Utils.angleMovementY(distance, directionDegrees) * delta
	return this
}

fun Vector2.setAngleMovement(distance: Float, directionDegrees: Float): Vector2 {
	x = Utils.angleMovementX(distance, directionDegrees)
	y = Utils.angleMovementY(distance, directionDegrees)
	return this
}

fun Vector2.setAngleMovement(x: Float, y: Float, directionDegrees: Float): Vector2 {
	this.x = Utils.angleMovementX(x, directionDegrees)
	this.y = Utils.angleMovementY(y, directionDegrees)
	return this
}

fun Rectangle.fixNegatives() {
	if (this.width < 0) {
		this.x += width
		this.width = -width
	}

	if (this.height < 0) {
		this.y += height
		this.height = -height
	}
}

fun Rectangle.middleX() = x + width / 2f

fun Rectangle.middleY() = y + height / 2f

fun Int.isKeyPressed() = Gdx.input.isKeyPressed(this)
fun Int.isKeyJustPressed() = Gdx.input.isKeyJustPressed(this)

fun Int.isButtonPressed() = Gdx.input.isButtonPressed(this)
fun Int.isButtonJustPressed() = Gdx.input.isButtonJustPressed(this)

fun <T : Actor> Cell<T>.padTopBottom(pad: Float): Cell<T> {
	this.padTop(pad)
	return this.padBottom(pad)
}

fun <E> Stack<E>.peekOrNull(): E? {
	if (size == 0) return null
	return peek()
}

fun <E> Stack<E>.popOrNull(): E? {
	if (size == 0) return null
	return pop()
}

fun Preferences.putBase64String(key: String, value: String) {
	this.putString(key, Base64.encode(value.toByteArray()))
}

fun Preferences.getBase64String(key: String, defValue: String? = null): String? {
	val value = this.getString(key, defValue)
	if (value == defValue) return value
	return String(Base64.decode(value))
}

fun IntRange.toIntArray(): IntArray {
	val size = this.last - this.first + 1
	var current = this.first
	return IntArray(size) { current++ }
}

fun intArrayOf(range: IntRange): IntArray {
	return intArrayOf(*range.toIntArray())
}

fun Actor.modifyToCatchInput() {
	this.touchable = Touchable.enabled

	this.addListener(object : ClickListener() {
		override fun clicked(event: InputEvent?, x: Float, y: Float) {

		}
	})
}

fun newColorFrom255RGB(red: Int, green: Int, blue: Int, alpha: Float = 1f): Color =
	Color(red / 255f, green / 255f, blue / 255f, alpha)

fun newColorFrom255RGB(redGreenBlue: Int, alpha: Float = 1f): Color =
	newColorFrom255RGB(redGreenBlue, redGreenBlue, redGreenBlue, alpha)

fun Actor.hasActionOfType(type: KClass<out Action>): Boolean {
	for (action in actions) if (type.isInstance(action)) return true
	return false
}

fun Actor.addAction(vararg actions: Action) {
	addAction(Actions.sequence(*actions))
}

fun Actor.setPosition(pos: Vector2) {
	this.setPosition(pos.x, pos.y)
}

fun Actor.setPosition(pos: Vector2, alignment: Int) {
	this.setPosition(pos.x, pos.y, alignment)
}

inline val Int.float: Float
	get() = this.toFloat()

inline val Double.float: Float
	get() = this.toFloat()

fun Exception.getFullString(): String {
	val sw = StringWriter()
	this.printStackTrace(PrintWriter(sw))
	return sw.toString()
}

inline fun <T, K> GdxMap<T, K>.getOrElse(key: T, defaultValue: (T) -> K): K {
	return get(key, null) ?: defaultValue(key)
}

inline fun <K, V> GdxMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
	val value = get(key)
	return if (value == null) {
		val answer = defaultValue()
		put(key, answer)
		answer
	} else {
		value
	}
}

fun JsonValue.addChildValue(name: String = "", value: Any) {
	when (value) {
		is Boolean -> addChild(name, JsonValue(value))
		is String -> addChild(name, JsonValue(value))
		is Int -> addChild(name, JsonValue(value.toLong()))
		is Long -> addChild(name, JsonValue(value))
		is Float -> addChild(name, JsonValue(value.toDouble()))
		is Double -> addChild(name, JsonValue(value))
		is JsonValue -> addChild(name, value)
		else -> throw RuntimeException("Tried to add value: $value (type: ${value.javaClass}) to json; but value has type not supported ")
	}
}

/** Will add the value but first will delete any other value with the same name, avoiding duplicates */
fun JsonValue.setChildValue(name: String = "", value: Any) {
	while (has(name))
		remove(name)

	addChildValue(name, value)
}

fun JsonValue.getChildValue(name: String = "", defaultValue: Any): Any? {
	return when (defaultValue) {
		is Boolean -> getBoolean(name, defaultValue)
		is String -> getString(name, defaultValue)
		is Int -> getInt(name, defaultValue)
		is Long -> getLong(name, defaultValue)
		is Float -> getFloat(name, defaultValue)
		is Double -> getDouble(name, defaultValue)
		is JsonValue -> getChild(name)
		else -> null
	}
}

fun <T : Actor> Cell<T>.padLeftRight(pad: Float): Cell<T> {
	this.padLeft(pad)
	return this.padRight(pad)
}

fun Table.padTopBottom(pad: Float): Table {
	this.padTop(pad)
	return this.padBottom(pad)
}

fun Table.padLeftRight(pad: Float): Table {
	this.padLeft(pad)
	return this.padRight(pad)
}

fun Table.addCollapsibleTable(topCheckBoxString: String, isCollapsed: Boolean): Table {
	val table = ExtTable()
	val collapsibleWidget = CollapsibleWidget(table, isCollapsed)

	add(
		VisCheckBox(
			topCheckBoxString,
			!collapsibleWidget.isCollapsed
		).also { it.onChange { collapsibleWidget.isCollapsed = !collapsibleWidget.isCollapsed } })
	row()
	add(collapsibleWidget)
	return table
}

private val tmpActorVector = Vector2()

fun Actor.stagePos(): Vector2 {
	tmpActorVector.set(0f, 0f)
	localToStageCoordinates(tmpActorVector)
	return tmpActorVector
}

fun Actor.stageX(): Float = stagePos().x
fun Actor.stageY(): Float = stagePos().y

fun Actor.parentPos(): Vector2 {
	tmpActorVector.set(x, y)
	localToParentCoordinates(tmpActorVector)
	return tmpActorVector
}

fun Actor.parentX(): Float = parentPos().x
fun Actor.parentY(): Float = parentPos().y

/** Percentage of application's height, rounded to closest integer */
fun percentOfAppHeight(percent: Float) = Utils.percentage100Int(Gdx.graphics.height.toFloat(), percent).toFloat()

/** Percentage of application's width, rounded to closest integer */
fun percentOfAppWidth(percent: Float) = Utils.percentage100Int(Gdx.graphics.width.toFloat(), percent).toFloat()

/** @return In viewport units */
fun percentOfViewportWidth(percent: Number, viewport: Viewport): Float {
	return (viewport.worldWidth * percent.toFloat()) * (viewport.camera as OrthographicCamera).zoom
}

/** @return In viewport units */
fun percentOfViewportHeight(percent: Number, viewport: Viewport): Float {
	return (viewport.worldHeight * percent.toFloat()) * (viewport.camera as OrthographicCamera).zoom
}

fun Color.set255(r: Int, g: Int, b: Int, a: Float = 1f) {
	set(r / 255f, g / 255f, b / 255f, a)
}

fun Rectangle.grow(growth: Float): Rectangle {
	x -= growth
	y -= growth
	width += growth * 2
	height += growth * 2
	return this
}

/** Rectangle constructor with default parameters */
fun newRectangle(
	x: Number = 0f, y: Number = 0f, width: Number = 0f, height: Number = 0f,
	centerX: Boolean = false, centerY: Boolean = false
): Rectangle {
	return Rectangle().also {
		it.x = x.toFloat() + if (centerX) -width.toFloat() / 2f else 0f
		it.y = y.toFloat() + if (centerY) -height.toFloat() / 2f else 0f
		it.width = width.toFloat(); it.height = height.toFloat()
	}
}

fun newRectangleCentered(x: Number = 0f, y: Number = 0f, width: Number = 0f, height: Number = 0f): Rectangle {
	return Rectangle().also {
		it.x = x.toFloat() - width.toFloat() / 2f; it.y = y.toFloat() - height.toFloat() / 2f
		it.width = width.toFloat(); it.height = height.toFloat()
	}
}

fun <T> ArrayList<T>.getRandom(): T = get(randomInt(size))
fun IntArray.getRandom(): Int = get(randomInt(size))
fun FloatArray.getRandom(): Float = get(randomInt(size))
fun <T> Array<T>.getRandom(): T = get(randomInt(size))

fun Number.map(inLow: Number, inHigh: Number, outLow: Number, outHigh: Number): Float {
	return map(this, inLow, inHigh, outLow, outHigh)
}

fun Number.format(
	normalDecimals: Int = 0, includeMagnitudes: Boolean = true, magnitudeDecimals: Int = 2,
	ignoreThousandsMagnitude: Boolean = false, ignoreMagnitudesUntil: Number = -1
): String {
	return Utils.formatNumber(
		this, normalDecimals, includeMagnitudes, magnitudeDecimals,
		ignoreThousandsMagnitude, ignoreMagnitudesUntil
	)
}

/** Extension to add support for doubles in Preferences. From https://stackoverflow.com/a/45412036 */
fun Preferences.putDouble(key: String, double: Double) =
	putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun Preferences.getDouble(key: String, default: Double) =
	java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))

fun <Type> GdxArray<Type>.getOrDefault(index: Int, default: Type): Type {
	if (index >= this.size || index < 0) return default
	return this[index] ?: default
}

infix fun <T : Actor> Table.add(actor: T): Cell<T> {
	return this.add(actor)
}

fun OrthographicCamera.moveToBottomLeft(x: Float, y: Float) {
	this.position.x = x + this.viewportWidth * this.zoom / 2f
	this.position.y = y + this.viewportHeight * this.zoom / 2f
}

fun OrthographicCamera.leftX(): Float {
	return position.x - viewportWidth * zoom / 2f
}

fun OrthographicCamera.rightX(): Float {
	return position.x + viewportWidth * zoom / 2f
}

fun OrthographicCamera.topY(): Float {
	return position.y + viewportHeight * zoom / 2f
}

fun OrthographicCamera.bottomY(): Float {
	return position.y - viewportHeight * zoom / 2f
}

fun OrthographicCamera.setLeftX(value: Float) {
	position.x = value + viewportWidth * zoom / 2f
}

fun OrthographicCamera.setRightX(value: Float) {
	position.x = value - viewportWidth * zoom / 2f
}

fun OrthographicCamera.setTopY(value: Float) {
	position.y = value - viewportHeight * zoom / 2f
}

fun OrthographicCamera.setBottomY(value: Float) {
	position.y = value + viewportHeight * zoom / 2f
}

/**
 * Automatically calls [Batch.begin] and [Batch.end].
 * @param action inlined. Executed after [Batch.begin] and before [Batch.end].
 */
inline fun <B : Batch> B.use(action: (B) -> Unit) {
	begin()
	action(this)
	end()
}

/**
 * Automatically calls [ShaderProgram.begin] and [ShaderProgram.end].
 * @param action inlined. Executed after [ShaderProgram.begin] and before [ShaderProgram.end].
 */
inline fun <S : ShaderProgram> S.use(action: (S) -> Unit) {
	begin()
	action(this)
	end()
}

inline fun <S : FrameBuffer> S.use(action: (S) -> Unit) {
	begin()
	action(this)
	end()
}

/** @return angle in degrees, from 0 to 360 */
fun Camera.getRotation(): Float {
	return (Math.atan2(up.x.toDouble(), up.y.toDouble()) * MathUtils.radiansToDegrees).toFloat() * -1f
}

fun OrthographicCamera.addRotation(degrees: Float) {
	setRotation(getRotation() + degrees)
}

/** @param degrees from 0 to 360 */
fun OrthographicCamera.setRotation(degrees: Float) {
	up.set(0f, 1f, 0f)
	direction.set(0f, 0f, -1f)
	rotate(-degrees)
}

fun Regex.getGroup(string: String, i: Int): String? {
	return find(string)?.groupValues?.getOrNull(i)
}

fun poolVector(): Vector2 = Pools.obtain(Vector2::class.java)
fun freeVector(vararg vector: Vector2): Unit = vector.forEach { Pools.free(it) }

inline fun poolVector(action: (Vector2) -> Unit) {
	poolVector().run {
		action(this)
		freeVector(this)
	}
}

/** Clamps input values to input range, then maps them to the output range.
 *
 * inLow must be lower than inHigh, but outLow can be bigger than outHigh  */
fun map(
	input: Number, inLow: Number, inHigh: Number, outLow: Number, outHigh: Number,
	interpolation: Easing = Easing.linear
): Float {
	return map(input.toFloat(), inLow.toFloat(), inHigh.toFloat(), outLow.toFloat(), outHigh.toFloat(), interpolation)
}

/** @see map */
fun map(
	input: Float, inLow: Float, inHigh: Float, outLow: Float, outHigh: Float,
	easing: Easing = Easing.linear
): Float {
	require(inLow <= inHigh) { "inLow must be lower than inHigh: $inLow < $inHigh" }
	var thisOutputLow = outLow
	var thisOutputHigh = outHigh
	if (input < inLow) return thisOutputLow
	if (input > inHigh) return thisOutputHigh

	var switched = false
	if (thisOutputLow > thisOutputHigh) {
		val temp = thisOutputHigh
		thisOutputHigh = thisOutputLow
		thisOutputLow = temp
		switched = true
	}

	val endInput = easing.apply(
		inLow, inHigh,
		(input - inLow) / (inHigh - inLow)
	) // map input inside low / high to 0 till 1

	val scale = (thisOutputHigh - thisOutputLow) / (inHigh - inLow)
	val value = (endInput - inLow) * scale + thisOutputLow

	return if (switched) {
		thisOutputLow - value + thisOutputHigh
	} else
		value
}

/** Like [map] but with a simple percentage as input values.
 * Point1 may be higher value than point2
 *
 * @param percent from 0 to 1 */
fun mapPercent(percent: Number, outLow: Number, outHigh: Number, interpolation: Easing = Easing.linear): Float {
	return map(percent, 0f, 1f, outLow, outHigh, interpolation)
}

fun clamp(value: Number, min: Number, max: Number): Float {
	val thisValue = value.toDouble()
	val thisMin = min.toDouble()
	val thisMax = max.toDouble()

	if (thisValue < thisMin) return thisMin.toFloat()
	return if (thisValue > thisMax) thisMax.toFloat() else thisValue.toFloat()
}

private val random = Random()

/** Includes 0, doesn't include length (From 0 to length - 1)  */
fun randomInt(length: Int): Int {
	return random.nextInt(length)
}

fun randomInt(minValue: Int, maxValue: Int): Int {
	val length = Math.abs(maxValue - minValue)
	val int = randomInt(length + 1)
	return minValue + int
}

fun randomInt(values: IntProgression): Int {
	return values.elementAt(randomInt(values.count()))
}

fun <T> randomChoose(vararg stuff: T): T {
	return stuff.get(randomInt(stuff.size))
}

fun randomChoose(vararg floats: Float): Float {
	return floats.get(randomInt(floats.size))
}

fun randomChoose(vararg ints: Int): Int {
	return ints.get(randomInt(ints.size))
}

fun randomAngle(): Float {
	return random.nextFloat() * 360f
}

fun randomFloat(length: Number): Float {
	return random.nextFloat() * length.toFloat()
}

fun randomFloat(length: Float): Float {
	return random.nextFloat() * length
}

/** Returns 1 or -1 */
fun randomSign(): Int {
	return if (randomFloat(1f) > 0.5) 1 else -1
}

fun randomChance(percent: Number = 0.5f): Boolean {
	var thisPercentageOfTrue = percent.toFloat()
	thisPercentageOfTrue = clamp(thisPercentageOfTrue, 0f, 1f)
	return randomFloat(1f) < thisPercentageOfTrue
}

fun randomFloat(min: Number, max: Number): Float {
	return map(randomFloat(1f), 0f, 1f, min, max)
}

fun BitmapFont.getRegionFromChar(char: Char): TextureRegion {
	val glyph = data.getGlyph(char)
	return TextureRegion(regions[glyph.page].texture, glyph.u, glyph.v, glyph.u2, glyph.v2).also {
		it.flip(
			false,
			true
		)
	}
}

/** Unlike built-in kotlin ranges, this one can iterate backwards automatically (in kotlin you need to manually change .. to downTo) */
infix fun Int.towards(to: Int): IntProgression {
	val step = if (this > to) -1 else 1
	return IntProgression.fromClosedRange(this, to, step)
}

/** To avoid "can't use nested iterator" error in [GdxArray] */
inline fun <T> GdxArray<out T>.forEachWithoutIterator(action: (T) -> Unit) {
	for (i in 0 until size) {
		val it = get(i); action(it)
	}
}

inline fun <T> GdxArray<out T>.forEachReversed(action: (T) -> Unit) {
	for (i in size - 1 downTo 0) {
		val it = get(i); action(it)
	}
}

fun Float.floor(): Int {
	return MathUtils.floor(this)
}

fun Float.floorf(): Float {
	return MathUtils.floor(this).toFloat()
}

fun Float.ceil(): Int {
	return MathUtils.ceil(this)
}

fun Float.round(): Int {
	return Math.round(this)
}

fun Vector2.add(value: Float) {
	x += value
	y += value
}

fun Vector2.setXY(value: Float) {
	set(value, value)
}

fun Vector2.temporarilyModify(f: (Vector2) -> Unit) {
	val oldX = x
	val oldY = y
	f(this)
	set(oldX, oldY)
}

/** Map operation that modifies the same mutable list instead of returning a new one */
private fun <E> MutableList<E>.mapInPlace(transform: (E) -> E): Collection<E> {
	for (i in this.indices) {
		this[i] = transform(this[i])
	}

	return this
}

private fun <E> Array<E>.mapInPlace(transform: (E) -> E): Array<E> {
	for (i in this.indices) {
		this[i] = transform(this[i])
	}

	return this
}

object Utils {
	fun roundToNearestScreenPixel(value: Float, viewport: Viewport): Float {
		val pix = 1f / (viewport.screenWidth / viewport.worldWidth) * (viewport.camera as OrthographicCamera).zoom
		return MathUtils.round(value / pix) * pix
	}

	inline fun spiralLoop(f: (x: Int, y: Int) -> Boolean) {
		var xAdd = 0
		var yAdd = 0
		var amount = 1
		var sign = 1
		var yTurn = false

		while (true) {
			if (f(xAdd, yAdd)) return

			if (yTurn)
				yAdd += sign
			else
				xAdd += sign

			if (!yTurn && xAdd == sign * amount) {
				yTurn = true
			} else if (yTurn && yAdd == sign * amount) {
				yTurn = false
				sign *= -1

				if (sign == 1)
					amount++
			}
		}
	}

	fun nextPowerOf2(a: Int): Int {
		var b = 1
		while (b < a) {
			b = b shl 1
		}
		return b
	}

	private val tmpPoly = Polygon(FloatArray(8))
	fun polygonOverlaps(polygon: Polygon, rect: Rectangle): Boolean {
		tmpPoly.vertices[0] = 0f
		tmpPoly.vertices[1] = 0f
		tmpPoly.vertices[2] = rect.width
		tmpPoly.vertices[3] = 0f
		tmpPoly.vertices[4] = rect.width
		tmpPoly.vertices[5] = rect.height
		tmpPoly.vertices[6] = 0f
		tmpPoly.vertices[7] = rect.height

		tmpPoly.setPosition(rect.x, rect.y)
		if (Intersector.overlapConvexPolygons(tmpPoly, polygon))
			return true
		return false
	}

	fun polygonOverlaps(polygon: Polygon, c: Circle): Boolean {
		val verts = polygon.transformedVertices
		val center = poolVector().set(c.x, c.y)

		val v1 = poolVector()
		val v2 = poolVector()

		val squareRadius = c.radius * c.radius

		var result = false
		for (i in verts.indices step 2) {
			if (i == 0) {
				if (Intersector.intersectSegmentCircle(
						v1.set(verts[verts.size - 2], verts[verts.size - 1]),
						v2.set(verts[i], verts[i + 1]), center, squareRadius
					)
				)
					result = true
			} else {
				if (Intersector.intersectSegmentCircle(
						v1.set(verts[i - 2], verts[i - 1]),
						v2.set(verts[i], verts[i + 1]), center, squareRadius
					)
				)
					result = true
			}

			if (result) break
		}

		freeVector(center, v1, v2)

		return result
	}

	fun hideCursor() {
		val pm = Pixmap(2, 2, Pixmap.Format.RGBA8888)
		pm.blending = Pixmap.Blending.None
		pm.setColor(Color(0x00000004))
		pm.fillRectangle(0, 0, 2, 2)
		Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0))
		pm.dispose()
	}

	fun setCursor(file: FileHandle) {
		val pm = Pixmap(file)
		Gdx.graphics.setCursor(Gdx.graphics.newCursor(pm, 0, 0))
		pm.dispose()
	}

	fun newCursor(file: FileHandle): Cursor {
		val pm = Pixmap(file)
		return Gdx.graphics.newCursor(pm, 0, 0)
	}

	fun roundFloat(value: Float, scale: Int): Float {
		var pow = 10
		for (i in 1 until scale) {
			pow *= 10
		}
		val tmp = value * pow
		val tmpSub = tmp - tmp.toInt()

		return (if (value >= 0)
			if (tmpSub >= 0.5f) tmp + 1 else tmp
		else
			if (tmpSub >= -0.5f) tmp else tmp - 1).toInt().toFloat() / pow
	}

	val gdxColors = GdxMap<String, Color>()

	fun randomColor(alpha: Float = 1f) = Color(randomFloat(1f), randomFloat(1f), randomFloat(1f), alpha)

	init {
		gdxColors.clear()
		gdxColors.put("CLEAR", Color.CLEAR)
		gdxColors.put("BLACK", Color.BLACK)
		gdxColors.put("WHITE", Color.WHITE)
		gdxColors.put("LIGHT_GRAY", Color.LIGHT_GRAY)
		gdxColors.put("GRAY", Color.GRAY)
		gdxColors.put("DARK_GRAY", Color.DARK_GRAY)
		gdxColors.put("BLUE", Color.BLUE)
		gdxColors.put("NAVY", Color.NAVY)
		gdxColors.put("ROYAL", Color.ROYAL)
		gdxColors.put("SLATE", Color.SLATE)
		gdxColors.put("SKY", Color.SKY)
		gdxColors.put("CYAN", Color.CYAN)
		gdxColors.put("TEAL", Color.TEAL)
		gdxColors.put("GREEN", Color.GREEN)
		gdxColors.put("CHARTREUSE", Color.CHARTREUSE)
		gdxColors.put("LIME", Color.LIME)
		gdxColors.put("FOREST", Color.FOREST)
		gdxColors.put("OLIVE", Color.OLIVE)
		gdxColors.put("YELLOW", Color.YELLOW)
		gdxColors.put("GOLD", Color.GOLD)
		gdxColors.put("GOLDENROD", Color.GOLDENROD)
		gdxColors.put("ORANGE", Color.ORANGE)
		gdxColors.put("BROWN", Color.BROWN)
		gdxColors.put("TAN", Color.TAN)
		gdxColors.put("FIREBRICK", Color.FIREBRICK)
		gdxColors.put("RED", Color.RED)
		gdxColors.put("SCARLET", Color.SCARLET)
		gdxColors.put("CORAL", Color.CORAL)
		gdxColors.put("SALMON", Color.SALMON)
		gdxColors.put("PINK", Color.PINK)
		gdxColors.put("MAGENTA", Color.MAGENTA)
		gdxColors.put("PURPLE", Color.PURPLE)
		gdxColors.put("VIOLET", Color.VIOLET)
		gdxColors.put("MAROON", Color.MAROON)
	}

	private val textureGroupsRegex = Regex("(.*)_(\\d+)")

	/** returns the index number if the textureName ends with _#. Returns -1 if no correct name format */
	fun textureNameIndex(textureName: String): Int {
		return textureGroupsRegex.find(textureName)?.groupValues?.get(2)?.toInt() ?: -1
	}

	fun textureNameWithoutIndex(textureName: String): String {
		return textureGroupsRegex.find(textureName)?.groupValues?.get(1) ?: textureName
	}

	//region NUMBER FORMAT
	private val magnitudes = arrayOf(
		"K", "M", "B", "T", "Qa", "Qi", "Sx", "Sp", "Oc", "No", "Dc", "UDc", "DDc",
		"TDc", "QaD", "QiD", "SxD", "SpD", "OcD", "NoD", "Vi"
	)

	private fun doTheFormat(number: Double, decimals: Int): String {
		return String.format(Locale.US, "%,." + Integer.toString(decimals) + "f", number)
	}

	/**
	 * Adapted from https://stackoverflow.com/a/30688774. Transforms input to Double and adds magnitudes to it. Should cover
	 * all range of values of Long but not all from Double
	 */

	fun formatNumber(
		number: Number, normalDecimals: Int = 0, includeMagnitudes: Boolean = true, magnitudeDecimals: Int = 2,
		ignoreThousandsMagnitude: Boolean = false, ignoreMagnitudesUntil: Number = -1,
		magnitudes: Array<String> = Utils.magnitudes
	): String {
		if (!includeMagnitudes) return doTheFormat(number.toDouble(), normalDecimals)
		fun doesNotNeedMagnitude(number: Double, originalNumber: Boolean = true): Boolean {
			return number < 1000 || (number < 1_000_000 && ignoreThousandsMagnitude && originalNumber)
					|| (number < ignoreMagnitudesUntil.toDouble() && ignoreMagnitudesUntil != -1 && originalNumber)
		}

		var thisNumber = number.toDouble()

		if (thisNumber <= -9200000000000000000L) {
			return "-9.2E"
		}

		if (doesNotNeedMagnitude(thisNumber)) return doTheFormat(thisNumber, normalDecimals)

		var i = 0
		while (true) {
//            if (thisNumber < 10000 && thisNumber % 1000 >= 100)
//                return ret + doTheFormat(thisNumber / 1000, magnitudeDecimals) +
//                        ',' + doTheFormat(thisNumber % 1000 / 100 + magnitudes[i].toDouble(), magnitudeDecimals)
			thisNumber /= 1000.0
			if (doesNotNeedMagnitude(thisNumber, false))
				return doTheFormat(thisNumber, magnitudeDecimals) + magnitudes[i]
			i++

			if (i > 99999)
				throw RuntimeException("Infinite loop in formatNumber. Number: $number")
		}
	}
	//endregion

	/** @return new value */
	fun approachAngle(current: Float, target: Float, incr: Float): Float {
		if (fixAngle(current) == fixAngle(target)) return current
		val angleDiff = angleDiffSigned(current, target)
		return current + Math.min(incr, angleDiff.absoluteValue) * sign(angleDiff)
	}

	fun fixAngle(angle: Float) = if (angle < 0f) angle + 360f else if (angle > 360f) angle - 360f else angle

	private val dummyVector2 = Vector2()
	private val dummyVector3 = Vector3()
	private val dummyVector4 = Vector2()

	fun projectPosition(
		pos: Vector2, originViewport: Viewport? = null, endViewport: Viewport? = null,
		flipY: Boolean = true, vector: Vector2 = dummyVector2
	): Vector2 {
		return projectPosition(pos.x, pos.y, originViewport, endViewport, flipY, vector)
	}

	fun projectPosition(
		x: Number, y: Number, originViewport: Viewport? = null, endViewport: Viewport? = null,
		flipY: Boolean = true, vector: Vector2 = dummyVector2
	): Vector2 {
		val coords = vector
		val thisX = x.toFloat()
		val thisY = y.toFloat()
		coords.set(thisX, thisY)
		originViewport?.project(coords)
		endViewport.ifNotNull {
			if (flipY) coords.y =
				Gdx.graphics.height - coords.y // looks like you need to flip it in this case, probably because
			// the unproject requires "opengl-oriented y"
			it.unproject(coords)
		}

		if (originViewport?.screenWidth == 0) {
			logWarning(
				"Projecting position of worldViewport with width 0. This will give unexpected results." +
						"\nCheck you are not calling the method before resize() was called (viewport still has no size)"
			)
		}

		return coords
	}

	fun projectX(x: Float, originCamera: Camera? = null, endCamera: Camera? = null): Float {
		dummyVector4.set(x, 0f)
		return projectPosition(dummyVector4.x, 0f, originCamera, endCamera).x
	}

	fun projectY(y: Float, originCamera: Camera? = null, endCamera: Camera? = null, flipY: Boolean = true): Float {
		dummyVector4.set(0f, y)
		return projectPosition(0f, dummyVector4.y, originCamera, endCamera, flipY).y
	}

	fun projectPosition(
		pos: Vector2,
		originCamera: Camera? = null,
		endCamera: Camera? = null,
		flipY: Boolean = true
	): Vector2 {
		return projectPosition(pos.x, pos.y, originCamera, endCamera, flipY)
	}

	fun projectPosition(
		x: Number,
		y: Number,
		originCamera: Camera? = null,
		endCamera: Camera? = null,
		flipY: Boolean = true
	): Vector2 {
		val coords = dummyVector3
		val thisX = x.toFloat()
		val thisY = y.toFloat()
		coords.set(thisX, thisY, 0f)
		originCamera?.project(coords)
		endCamera.ifNotNull {
			if (flipY) coords.y =
				Gdx.graphics.height - coords.y // looks like you need to flip it in this case, probably because
			// the unproject requires "opengl-oriented y"
			it.unproject(coords)
		}

		if (originCamera?.viewportWidth == 0f) {
			logWarning(
				"Projecting position of worldViewport with width 0. " +
						"This will give unexpected results." +
						"\nCheck you are not calling the method before resize() was called (viewport still has no size)"
			)
		}

		dummyVector2.set(coords.x, coords.y)
		return dummyVector2
	}

	fun isWindowFocused(): Boolean {
		return GLFW.glfwGetWindowAttrib((Gdx.graphics as Lwjgl3Graphics).window.windowHandle, GLFW.GLFW_FOCUSED) == 1
	}

	fun clearScreen(r: Int = 58, g: Int = 68, b: Int = 102) {
		Gdx.gl.glClearColor(r / 255f, g / 255f, b / 255f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
	}

	fun clearScreen(color: Color) {
		Gdx.gl.glClearColor(color.r, color.g, color.b, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
	}

	fun scene2dActionShake(maximumMovingQuantity: Float, originalX: Float = 0f, originalY: Float = 0f): Action {
		fun random(): Float {
			return randomFloat(0f, maximumMovingQuantity) * randomSign()
		}

		fun shake(): Action {
			return Actions.sequence(
				Actions.moveBy(random(), random(), 0.03f, Easing.pow3Out),
				Actions.moveTo(originalX, originalY, 0.02f, Easing.pow3Out)
			)
		}

		return Actions.sequence(shake(), shake(), shake(), shake())
	}

	fun getDecimalPart(number: Float): Float {
		return number - number.toInt()
	}

	/** applies a percentage from 0 to 100 to the number */

	fun percentage100(number: Number, percentage: Number): Float {
		return (number.toDouble() * (percentage.toDouble() / 100.0)).toFloat()
	}

	fun percentage100Int(number: Number, percentage: Number): Int {
		return percentage100(number, percentage).toInt()
	}

	fun removeExtensionFromFilename(filename: String): String {
		val i = filename.lastIndexOf(".")
		return if (i >= 0)
			filename.substring(0, i)
		else
			filename
	}

	fun lerpAngle(speed: Float, inputAngle: Float, targetAngle: Float): Float {
		val diff = angleDiffSigned(inputAngle, targetAngle)
		var qty = speed
		if (qty > Math.abs(diff)) qty = Math.abs(diff)
		qty *= Math.signum(diff)
		return inputAngle + qty
	}

	fun angleMovementX(distance: Float, angle: Float): Float {
		var thisDirectionDegrees = angle
		if (thisDirectionDegrees < 0) {
			thisDirectionDegrees += 360f
		}
		return roundFloat(distance * MathUtils.cosDeg(angle), 5)
	}

	fun angleMovementY(distance: Float, angle: Float): Float {
		var thisDirectionDegrees = angle
		if (thisDirectionDegrees < 0) {
			thisDirectionDegrees += 360f
		}
		return roundFloat(distance * MathUtils.sinDeg(angle), 5)
	}

	/**
	 * Length (angular) of a shortest way between two angles.
	 * It will be in range [-180, 180] (signed).
	 */

	fun angleDiffSigned(sourceAngle: Float, targetAngle: Float): Float {
		val thisSourceAngle = fixAngle(sourceAngle) * MathUtils.degreesToRadians
		val thisTargetAngle = fixAngle(targetAngle) * MathUtils.degreesToRadians
		return (atan2(
			sin((thisTargetAngle - thisSourceAngle)),
			cos((thisTargetAngle - thisSourceAngle))
		)) * MathUtils.radiansToDegrees
	}

	fun angleDiffNotSigned(sourceAngle: Float, targetAngle: Float): Float {
		return abs(angleDiffSigned(sourceAngle, targetAngle))
	}

	fun angleAndDistanceBetween(start: Vector2, end: Vector2): Pair<Float, Float> {
		return Pair(angleBetween(start, end), distanceBetween(start, end))
	}

	fun angleAndDistanceBetween(x1: Float, y1: Float, x2: Float, y2: Float): Pair<Float, Float> {
		return Pair(angleBetween(x1, y1, x2, y2), distanceBetween(x1, y1, x2, y2))
	}

	fun angleBetween(start: Vector2, end: Vector2): Float {
		return angleBetween(start.x, start.y, end.x, end.y)
	}

	fun angleBetween(x1: Float, y1: Float, x2: Float, y2: Float): Float {
		var angle = Math.toDegrees(atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())).toFloat()
		if (angle < 0) angle += 360f
		return angle
	}

	fun distanceBetween(x1: Float, y1: Float, x2: Float, y2: Float): Float {
		return Math.hypot((x1 - x2).toDouble(), (y1 - y2).toDouble()).toFloat()
	}

	fun distanceBetween(point1: Vector2, point2: Vector2): Float {
		return distanceBetween(point1.x, point1.y, point2.x, point2.y)
	}

	fun getClosestNumberInList(number: Float, list: FloatArray): Float {
		var ret = list[0]
		var diff = Math.abs(ret - number)
		for (i in 1 until list.size) {
			if (ret != list[i]) {
				val newDiff = Math.abs(list[i] - number)
				if (newDiff < diff) {
					ret = list[i]
					diff = newDiff
				}
			}
		}
		return ret
	}

	/** Using VisUI, creates a slider which allows to change a value. Pass a function to easily update a variable with the slider's value
	 * Uses ExtLabels with the default font */
	fun visUI_valueChangingSlider(
		valueName: String, minValue: Float = 0f, maxValue: Float = 1f, startingValue: Float = 0.5f,
		stepSize: Float = 0.01f, decimals: Int = 2, width: Float = -1f, updateValue: (value: Float) -> Unit
	): ExtTable {
		var changed = false
		return ExtTable().also {
			it.pad(10f)
			it.add(ExtLabel(valueName)).center()

			it.row()

			val visSlider = VisSlider(minValue, maxValue, stepSize, false).also {
				it.value = startingValue
			}

			fun format(number: Number): String {
				return formatNumber(number, decimals, true, 3, true)
			}

			it.add(Table().also {
				it.add(ExtLabel(format(minValue)))

				if (width >= 0f)
					it.add(visSlider).width(width)
				else
					it.add(visSlider)

				it.add(ExtLabel(format(maxValue)))
			})

			it.row()
			it.add(ExtLabel().also {
				it.onUpdateText = { visSlider.value.toString() }
			}).center()

			visSlider.onChange { updateValue(visSlider.value) }
		}
	}

	fun visUI_spinnerWithSlider(
		valueName: String,
		minValue: Float = 0f,
		maxValue: Float = 1f,
		decimals: Int = 2,
		stepSize: Float = 1f / (10 * decimals),
		getValue: () -> Float,
		updateValue: (value: Float) -> Unit
	): ExtTable {
		return ExtTable().also {
			it.pad(10f)
//            it.add(ExtLabel(valueName)).center()

			val startingValue = getValue()
			it.add(VisLabel(valueName))

			val visSlider = object : VisSlider(minValue, maxValue, stepSize, false) {
				init {
					value = startingValue

					addListener(object : InputListener() {
						override fun touchDown(
							event: InputEvent?,
							x: Float,
							y: Float,
							pointer: Int,
							button: Int
						): Boolean {
							return false
						}
					})
				}

				override fun act(delta: Float) {
					super.act(delta)

					value = getValue()
				}
			}

			it.add(
				visUI_customFloatSpinner("",
					SimpleFloatSpinnerModel(startingValue, minValue, maxValue, stepSize, decimals),
					{ updateValue(it) },
					{ visSlider.value })
			)

			it.add(ExtTable().also {
				fun format(number: Number): String {
					return formatNumber(number, decimals, true, 3, true)
				}

				it.add(Table().also {
					it.add(ExtLabel(format(minValue)))

					it.add(visSlider)

					it.add(ExtLabel(format(maxValue)))
				})

				it.row()
				it.add(ExtLabel().also {
					it.onUpdateText = { format(visSlider.value).toString() }
				}).center()

				visSlider.onChange { updateValue(visSlider.value) }
			})
		}
	}

	fun visUI_customIntSpinner(
		name: String, model: IntSpinnerModel, onChange: (Int) -> Unit,
		getUpdatedValue: () -> Int
	): Spinner {
		return object : Spinner("", model) {
			var lastChange = 0

			init {
				onChange {
					onChange(model.value)
					lastChange = model.value
				}
			}

			override fun act(delta: Float) {
				super.act(delta)

				if (lastChange != getUpdatedValue())
					model.setValue(getUpdatedValue(), true)
			}
		}
	}

	fun visUI_customIntSpinner(
		name: String, stepSize: Int, onChange: (Int) -> Unit,
		getUpdatedValue: () -> Int
	): Spinner {
		return visUI_customIntSpinner(
			name,
			IntSpinnerModel(getUpdatedValue(), Int.MIN_VALUE, Int.MAX_VALUE, stepSize), onChange, getUpdatedValue
		)
	}

	fun visUI_customFloatSpinner(
		name: String,
		model: SimpleFloatSpinnerModel,
		onChange: (Float) -> Unit,
		getUpdatedValue: () -> Float
	): Spinner {
		return object : Spinner("", model) {
			var lastChange = 0f

			init {
				onChange {
					onChange(model.value)
					lastChange = model.value
				}
			}

			override fun act(delta: Float) {
				super.act(delta)

				if (lastChange != getUpdatedValue())
					model.setValue(getUpdatedValue(), true)
			}
		}
	}

	fun visUI_customFloatSpinner(
		name: String,
		stepSize: Float,
		onChange: (Float) -> Unit,
		getUpdatedValue: () -> Float
	): Spinner {
		return visUI_customFloatSpinner(
			name,
			SimpleFloatSpinnerModel(getUpdatedValue(), Float.MIN_VALUE, Float.MAX_VALUE, stepSize),
			onChange,
			getUpdatedValue
		)
	}

	/** VisUI checkbox, but the text is in a [ExtLabel] */
	fun visUI_customCheckBox(text: String, checkedCondition: () -> Boolean, textPadLeft: Float = 5f): Table {
		return Table().also {
			it.add(object : VisCheckBox("", checkedCondition()) {
				init {
					it.add(ExtLabel(text).also { it.setAlignment(Align.left) }).padLeft(textPadLeft)
				}

				override fun act(delta: Float) {
					super.act(delta)

					if (checkedCondition() != isChecked) {
						setProgrammaticChangeEvents(false)
						isChecked = !isChecked
						setProgrammaticChangeEvents(true)
					}
				}
			})
		}
	}
}

/** Only owner (if it exists) can modify the array */
class ReadOnlyGdxArray<T>(val owner: Any? = null) : GdxArray<T>() {
	private var blockChanges = true

	fun modify(owner: Any, f: (ReadOnlyGdxArray<T>) -> Unit) {
		if (this.owner == null || owner != this.owner) throw RuntimeException("Only ${this.owner} can modify this array")

		blockChanges = false
		f(this)
		blockChanges = true
	}

	private fun throwModifyException() {
		throw RuntimeException("Read-only array is locked")
	}

	override fun addAll(array: com.badlogic.gdx.utils.Array<out T>?) {
		if (blockChanges) throwModifyException()
		super.addAll(array)
	}

	override fun addAll(array: com.badlogic.gdx.utils.Array<out T>?, start: Int, count: Int) {
		if (blockChanges) throwModifyException()
		super.addAll(array, start, count)
	}

	override fun addAll(vararg array: T) {
		if (blockChanges) throwModifyException()
		super.addAll(*array)
	}

	override fun addAll(array: Array<out T>?, start: Int, count: Int) {
		if (blockChanges) throwModifyException()
		super.addAll(array, start, count)
	}

	override fun add(value: T) {
		if (blockChanges) throwModifyException()
		super.add(value)
	}

	override fun add(value1: T, value2: T) {
		if (blockChanges) throwModifyException()
		super.add(value1, value2)
	}

	override fun add(value1: T, value2: T, value3: T) {
		if (blockChanges) throwModifyException()
		super.add(value1, value2, value3)
	}

	override fun add(value1: T, value2: T, value3: T, value4: T) {
		if (blockChanges) throwModifyException()
		super.add(value1, value2, value3, value4)
	}

	override fun clear() {
		if (blockChanges) throwModifyException()
		super.clear()
	}

	override fun insert(index: Int, value: T) {
		if (blockChanges) throwModifyException()
		super.insert(index, value)
	}

	override fun pop(): T {
		if (blockChanges) throwModifyException()
		return super.pop()
	}

	override fun removeAll(array: com.badlogic.gdx.utils.Array<out T>?, identity: Boolean): Boolean {
		if (blockChanges) throwModifyException()
		return super.removeAll(array, identity)
	}

	override fun shuffle() {
		if (blockChanges) throwModifyException()
		super.shuffle()
	}

	override fun shrink(): Array<T> {
		if (blockChanges) throwModifyException()
		return super.shrink()
	}

	override fun sort() {
		if (blockChanges) throwModifyException()
		super.sort()
	}

	override fun sort(comparator: Comparator<in T>?) {
		if (blockChanges) throwModifyException()
		super.sort(comparator)
	}

	override fun set(index: Int, value: T) {
		if (blockChanges) throwModifyException()
		super.set(index, value)
	}

	override fun setSize(newSize: Int): Array<T> {
		if (blockChanges) throwModifyException()
		return super.setSize(newSize)
	}

	override fun removeRange(start: Int, end: Int) {
		if (blockChanges) throwModifyException()
		super.removeRange(start, end)
	}

	override fun removeValue(value: T, identity: Boolean): Boolean {
		if (blockChanges) throwModifyException()
		return super.removeValue(value, identity)
	}

	override fun reverse() {
		if (blockChanges) throwModifyException()
		super.reverse()
	}

	override fun removeIndex(index: Int): T {
		if (blockChanges) throwModifyException()
		return super.removeIndex(index)
	}

	override fun resize(newSize: Int): Array<T> {
		if (blockChanges) throwModifyException()
		return super.resize(newSize)
	}
}