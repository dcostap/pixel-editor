package dev.dcostap

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import dev.dcostap.Debug.log
import dev.dcostap.Shaders.disposeShaders
import dev.dcostap.Shaders.loadShaders
import dev.dcostap.utils.Utils
import dev.dcostap.utils.getOrPut
import com.kotcrab.vis.ui.VisUI
import com.rafaskoberg.gdx.typinglabel.TypingConfig
import ktx.collections.*
import me.xdrop.fuzzywuzzy.FuzzySearch

object Assets2D : Disposable {
	val cursorNormal = Utils.newCursor(Gdx.files.internal("cursors/cursor.png"))

	val fontDare: BitmapFont by lazy { dev.dcostap.Assets2D.loadExternalBitmapFont("darePixel_s10") }
	val fontDareS: BitmapFont by lazy { dev.dcostap.Assets2D.loadExternalBitmapFont("darePixelSmall_s7") }
	val fontEquipment: BitmapFont by lazy { dev.dcostap.Assets2D.loadExternalBitmapFont("equipment_s16") }
	val fontEquipmentDark: BitmapFont by lazy { dev.dcostap.Assets2D.loadExternalBitmapFont("equipment2_s16") }
	val fontEquipmentNoShadow: BitmapFont by lazy { dev.dcostap.Assets2D.loadExternalBitmapFont("equipment_s16_noShadow") }
	val fontDareOutline: BitmapFont by lazy { dev.dcostap.Assets2D.loadExternalBitmapFont("fontDareOutline") }
	val fontDareSmallOutline: BitmapFont by lazy { dev.dcostap.Assets2D.loadExternalBitmapFont("fontDareSmallOutline") }
	val fontDareSmallOutline2x: BitmapFont by lazy {
		dev.dcostap.Assets2D.loadBitmapFont(
			"fontDareSmallOutline",
			scale = 2f
		)
	}
	val fontEquipmentOutline: BitmapFont by lazy { dev.dcostap.Assets2D.loadExternalBitmapFont("equipment_outline") }

	val pixel: TextureRegion by lazy {
		dev.dcostap.Assets2D.getRegion("pixel")
	}

	private fun finishLoading() {
		TypingConfig.INTERVAL_MULTIPLIERS_BY_CHAR.put('\n', 3f)
		TypingConfig.INTERVAL_MULTIPLIERS_BY_CHAR.put('.', 4.8f)
		TypingConfig.DEFAULT_SPEED_PER_CHAR = 0.03f
	}

	val visDefaultFont
		get() = VisUI.getSkin().getFont("default-font")

	val visSmallFont
		get() = VisUI.getSkin().getFont("small-font")


	private fun loadBitmapFont(
		name: String,
		pixelatedFiltering: Boolean = true,
		scale: Float = 1f,
		fixedWidthNumbers: Boolean = true
	): BitmapFont {
		return BitmapFont(Gdx.files.internal(dev.dcostap.AssetsExporter.outputFolder + "/fonts/$name.fnt"),
			dev.dcostap.Assets2D.getRegion(name)
		).also {
			if (pixelatedFiltering) {
				it.region.texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
				it.setUseIntegerPositions(false)
			}

			it.data.setScale(scale)

			if (fixedWidthNumbers) {
				it.setFixedWidthGlyphs("1234567890")
			}
		}
	}

	lateinit var atlas: TextureAtlas
		private set

	fun reloadTextureAtlas() {
		dev.dcostap.AssetsExporter.exportAssets()
		dev.dcostap.Assets2D.atlas = TextureAtlas(dev.dcostap.Assets2D.atlasLocation)
		dev.dcostap.Assets2D.cachedIndividualRegionsInsideGroups.clear()
		dev.dcostap.Assets2D.regionGroups.clear()
		dev.dcostap.Assets2D.regions.clear()
		dev.dcostap.Assets2D.setupTextureAtlas()
	}

	class AnimFrameRegion(val frameSpeedMultiplier: Float, region: TextureAtlas.AtlasRegion?) :
		TextureAtlas.AtlasRegion(region)

	/**
	 * Finds image in the atlas ignoring extensions. If the name ends with "_#" it loads it from the array that texturePacker creates
	 */
	fun findRegionFromRawImageName(rawImageName: String): TextureRegion {
		Utils.removeExtensionFromFilename(rawImageName)

		val i = rawImageName.lastIndexOf('_')
		if (i != -1) {
			try {
				val index = Integer.valueOf(rawImageName.substring(i + 1))
				val realName = rawImageName.substring(0, i)
				return dev.dcostap.Assets2D.atlas.findRegion(realName, index)
			} catch (exception: Exception) {

			}
		}

		return dev.dcostap.Assets2D.getRegion(rawImageName)
	}

	private class IndividualTextureInsideGroup(val groupName: String, val index: Int)

	private val regions = HashMap<String, TextureAtlas.AtlasRegion>()

	private val regionGroups = HashMap<String, GdxArray<TextureAtlas.AtlasRegion>>()

	/** When textures end with _# they are treated as a group by texturePacker. Original names are cached here
	 * so if you access a texture by its original name you still get the result. This helps with Tiled exported maps, which
	 * will refer to images in tilesets with their original name.
	 *
	 * This might give unexpected behavior if there are  multiple textures with same base name ("spr.png" & "spr_0.png") */
	private val cachedIndividualRegionsInsideGroups = HashMap<String, dev.dcostap.Assets2D.IndividualTextureInsideGroup>()

	fun hasRegion(name: String): Boolean {
		return (dev.dcostap.Assets2D.cachedIndividualRegionsInsideGroups.containsKey(name)
				|| dev.dcostap.Assets2D.regions.containsKey(name) || dev.dcostap.Assets2D.regionGroups.containsKey(name))
	}

	fun getRegion(name: String): TextureAtlas.AtlasRegion {
		dev.dcostap.Assets2D.cachedIndividualRegionsInsideGroups.get(name)?.also {
			return dev.dcostap.Assets2D.getRegions(it.groupName)[it.index]
		}

		return dev.dcostap.Assets2D.regions.getOrElse(name) {
			var findings = mapOf<Int, String>()

			for (i in name.length - 1 downTo 3) {
				val list = dev.dcostap.Assets2D.regions.keys + dev.dcostap.Assets2D.cachedIndividualRegionsInsideGroups.keys
				findings += list.map { FuzzySearch.ratio(name, it) to it }
			}

			findings = findings.filterKeys { it > 20 }

			throw RuntimeException(
				"Region: '$name' not found on atlas"
						+ if (findings.isEmpty()) "" else "\n__________\nRegions with similar names:\n${
					findings.toSortedMap().values.reversed().joinToString(
						"\n"
					)
				}\n__________"
			)
		}
	}

	private val ninePatches = GdxMap<String, NinePatch>()

	fun getNinePatch(name: String): NinePatch = dev.dcostap.Assets2D.ninePatches.getOrPut(name) { dev.dcostap.Assets2D.atlas.createPatch(name) }

	fun getRegionOrNull(name: String): TextureAtlas.AtlasRegion? {
		dev.dcostap.Assets2D.cachedIndividualRegionsInsideGroups.get(name)?.also {
			dev.dcostap.Assets2D.getRegionsOrNull(it.groupName)?.also { array ->
				return array[it.index]
			}
			return null
		}

		return dev.dcostap.Assets2D.regions.get(name)
	}

	fun getRegionOrElse(name: String, f: () -> TextureAtlas.AtlasRegion?): TextureAtlas.AtlasRegion? {
		dev.dcostap.Assets2D.cachedIndividualRegionsInsideGroups.get(name)?.also {
			var spr: TextureAtlas.AtlasRegion?
			try {
				spr = dev.dcostap.Assets2D.getRegions(it.groupName)[it.index]
			} catch (exc: RuntimeException) {
				spr = f()
			}
			return spr
		}

		return dev.dcostap.Assets2D.regions.getOrElse(name) {
			var texture = dev.dcostap.Assets2D.atlas.findRegion(name)
			if (texture == null) texture = f()
			texture
		}
	}

	fun getRegions(name: String): GdxArray<TextureAtlas.AtlasRegion> {
		return dev.dcostap.Assets2D.regionGroups.getOrElse(name) {
			throw RuntimeException("Group of regions: '$name' not found on atlas. ( .findRegions() )")
		}
	}

	fun getRegionsOrNull(name: String): GdxArray<TextureAtlas.AtlasRegion>? {
		return dev.dcostap.Assets2D.regionGroups.get(name)
	}

	private fun getRegionNames(): String {
		return dev.dcostap.Assets2D.regions.keys.joinToString { it }
	}

	private val assetManager = AssetManager()

	override fun dispose() {
		dev.dcostap.Assets2D.assetManager.dispose()
		disposeShaders()
	}

	private var finishedLoading = false

	const val atlasLocation: String = dev.dcostap.AssetsExporter.outputFolder + "/atlas/atlas.atlas"

	private fun loadExternalBitmapFont(name: String): BitmapFont {
		return BitmapFont(Gdx.files.internal(dev.dcostap.AssetsExporter.outputFolder + "/fonts/$name.fnt"),
			dev.dcostap.Assets2D.getRegion(name)
		)
	}

	private var isLoadingInitiated = false

	/**
	 * @return whether it finished loading
	 */
	fun processAssetLoading(): Boolean {
		if (!dev.dcostap.Assets2D.isLoadingInitiated) {
			dev.dcostap.Assets2D.assetManager.load(dev.dcostap.Assets2D.atlasLocation, TextureAtlas::class.java)
			dev.dcostap.Assets2D.isLoadingInitiated = true
		}
		if (dev.dcostap.Assets2D.finishedLoading) return true

		if (dev.dcostap.Assets2D.assetManager.update()) {
			dev.dcostap.Assets2D.atlas = dev.dcostap.Assets2D.assetManager.get(dev.dcostap.Assets2D.atlasLocation, TextureAtlas::class.java)

			dev.dcostap.Assets2D.setupTextureAtlas()

			dev.dcostap.Assets2D.finishLoading()

			VisUI.load()

			loadShaders()

			dev.dcostap.Assets2D.finishedLoading = true
			return true
		}

		return false
	}

	private fun setupTextureAtlas() {
		val regex = Regex("(.*?)_n(\\d+)")
		val speedMultRegex = Regex("-(\\d+)=([+-]?([0-9]*[.])?[0-9]+)")
		var single = 0
		var anim = 0
		val origNameToNewAnim = GdxMap<dev.dcostap.Assets2D.AnimFrameRegion, String>()

		for (region in dev.dcostap.Assets2D.atlas.regions) {
			var name = region.name

			if (region.name.matches(regex)) {
				anim++
				regex.find(region.name)?.let {
					name = it.groupValues.get(1)
					var mult = 1f
					val index = it.groupValues.get(2).toInt()

					for (match in speedMultRegex.findAll(region.name)) {
						name = name.replace(match.groupValues[0], "")
						if (match.groupValues[1].toInt() == index) mult = match.groupValues[2].toFloat()
					}

//					log("\t...region is part of animation. Final name = $name. index=$index. speed mult=$mult")

					val anim = dev.dcostap.Assets2D.AnimFrameRegion(mult, region)
					origNameToNewAnim.put(anim, name + "_n$index")
					dev.dcostap.Assets2D.regionGroups.getOrPut(name) { GdxArray() }.let {
						if (it.size <= index) it.setSize(index + 1)
						it.set(index, anim)
					}
				}
			} else {
				single++
				dev.dcostap.Assets2D.regions.put(name, region)
			}
		}

		// delete null regions in the middle
		// there can be gaps in textures out of a tileset: [spr_n1, spr_n2, null, null, spr_n5, ...]
		dev.dcostap.Assets2D.regionGroups.replaceAll { k, v -> v.filterNotNull().toGdxArray() }

		// now map the original name of each texture to the new index, after null regions in the middle are eliminated
		// we need to delete those gaps in the actual regionGroups, but we also need the original name (spr_n5)
		dev.dcostap.Assets2D.regionGroups.forEach { key, it ->
			it.forEachIndexed { index, it ->
				if (origNameToNewAnim.containsKey(it as dev.dcostap.Assets2D.AnimFrameRegion))
					dev.dcostap.Assets2D.cachedIndividualRegionsInsideGroups.put(
						origNameToNewAnim.get(it),
						dev.dcostap.Assets2D.IndividualTextureInsideGroup(key, index)
					)
			}
		}

		log("$single single regions added; $anim animations added")
	}
}

