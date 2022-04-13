package dev.dcostap

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.tools.texturepacker.TexturePacker
import dev.dcostap.Debug.log
import dev.dcostap.utils.getGroup
import dev.dcostap.utils.newColorFrom255RGB
import java.io.File
import java.math.BigInteger
import java.nio.file.Paths

/** Created by Darius on 02-Mar-19. */
object AssetsExporter {
	const val imagesOrigin = "../assets_raw/atlas"
	const val blenderFilesOrigin = "../assets_raw/3d"
	const val outputFolder = "generated"

	/** @see process2DAssets */
	val ignoreBlankRegions = true

	fun exportAssets(include2DAssets: Boolean = true) {
		if (Gdx.app.type != Application.ApplicationType.Desktop) return

		if (include2DAssets)
			process2DAssets()
	}

	/** Will use texturePacker to pack all images, only if files have changed or atlas output files are not created.
	 * Before letting texturePacker work, applies special processing to images found there.
	 *
	 * Images starting with _ are deleted automatically
	 *
	 * Images which include [-crop]-WxH[-n#] in the name will be considered a tileset
	 *  - The image will be sliced into image regions following a grid of values W (width) and H (height)
	 *  - Resulting regions will be saved as new png files
	 *  - Original image will be deleted when finished
	 *  - By default fully transparent regions might be ignored [ignoreBlankRegions]. If you want to include transparent regions, use
	 *  optional -n# parameter to declare the number of regions that should be created. Created images will include fully
	 *  transparent regions but only n number of regions will be sliced
	 *  - Regions are counted starting from top-left going right then 1 new row bottom until finished.
	 *  If image border isn't of size W / H it will be ignored
	 *
	 *  Images which include one # character in the name, will ignore the optional n# argument and...
	 *  - Each new row will be a new animation set
	 *  - Each new animation set will have the name with the index (starting with 0) replacing the first #
	 *
	 * Folders with name ending in -merge will generate one .png image that combines all images inside,
	 * with the name of the folder without -merge
	 *
	 * Pixels of color 4 20 69 (R G B) are ignored
	 *
	 * Font files will automatically be copied to assets/skins folder
	 * */
	private fun process2DAssets() {
		log("Checking for image changes...")

		val isModified = TexturePacker.isModified(imagesOrigin, Paths.get(outputFolder, "atlas").toString(), "atlas", TexturePacker.Settings())

		if (isModified) {
			log("Packing images...")

			// delete images starting with _
			fun invalidFile(file: File) = (file.name.endsWith(".png") && file.name.startsWith("_"))

			val tilesetRegex = "(.*?)(-crop)?-(\\d+)x(\\d+)(-n(\\d+))?(-8bitmin)?".toRegex()
			val tilesetHashTag = ".*?(#).*".toRegex()
			fun isTilesetFile(file: File) = (file.name.endsWith(".png") && file.nameWithoutExtension.matches(tilesetRegex))

			val mergeFolderRegex = "(.*?)-merge".toRegex()

			var firstFontFile = true
			fun fontFile(file: File) = file.name.endsWith(".fnt")

			val ignoredColor = Color.rgba8888(newColorFrom255RGB(4, 20, 69))

			val merges = HashMap<String, ArrayList<File>>()

			for (file in File(imagesOrigin).walk()) {
				if (file.isFile && file.parentFile.isDirectory && file.parentFile.name.contains(mergeFolderRegex)) {
					merges.getOrPut(mergeFolderRegex.getGroup(file.parentFile.name, 1)!!) { ArrayList() }.add(file)
				}
			}

			for (entry in merges.entries) {
				val merge = entry.value
				merge.sortWith(Comparator { o1, o2 -> o2.lastModified().compareTo(o1.lastModified()) })

				if (merge.isNotEmpty()) {
					var maxWidth = 0
					var maxHeight = 0
					for (file in merge) {
						Pixmap(FileHandle(file)).also {
							if (it.width > maxWidth) maxWidth = it.width
							if (it.height > maxHeight) maxHeight = it.height
						}
					}

					val newImage = Pixmap(maxWidth, maxHeight, Pixmap.Format.RGBA8888)
					for (file in merge) {
						if (file.isFile) {
							newImage.blending = Pixmap.Blending.SourceOver

							val current = Pixmap(FileHandle(file))
							for (xx in 0 until current.width) {
								for (yy in 0 until current.height) {
									val pixel = current.getPixel(xx, yy)
									if (ignoredColor != pixel)
										newImage.drawPixel(xx, yy, pixel)
								}
							}

							current.dispose()
							file.delete()
						}
					}

					val finalName = mergeFolderRegex.getGroup(merge.first().parentFile.name, 1) + ".png"
					val parentPath = merge.first().parentFile.parentFile.path
					PixmapIO.writePNG(FileHandle("$parentPath/$finalName"), newImage)
					merge.first().parentFile.delete()
				}
			}

			for (file in File(imagesOrigin).walk()) {
				if (file.isFile) {
					if (invalidFile(file)) {
						log("Deleting ${file.name}")
						file.delete()
					} else if (isTilesetFile(file)) {
						log("Detected tileSet image: ${file.name}")
						val find = tilesetRegex.find(file.nameWithoutExtension)
						find!!
						val origName = find.groupValues.get(1)

						val cropIt = find.groupValues.get(2) != ""
						val width = find.groupValues.get(3).toInt()
						val height = find.groupValues.get(4).toInt()

						val frameNumber = find.groupValues.get(6)

						//todo finish this
						val isTilesetMinimal = false //find.groupValues.getOrNull(7) != null

						var optionalNumberOfFrames = if (frameNumber == "") null else frameNumber.toInt()

						// slice the texture into new textures from region of width, height
						val image = Pixmap(FileHandle(file))
						image.blending = Pixmap.Blending.None
						var x = 0
						var y = 0
						var i = 0

						var name = origName

						var replaceHashTag = false
						var replaceIndex = 0

						fun newRowNewImageAnim() {
							name = origName.replaceFirst("#", replaceIndex.toString())
							replaceIndex++
							i = 0
							optionalNumberOfFrames = null
						}

						if (tilesetHashTag.matches(name)) {
							replaceHashTag = true
							newRowNewImageAnim()
						}

						fun isPixelTransparent(pixel: Int): Boolean {
							val b: Byte = 0
							return !(ignoredColor != pixel && BigInteger.valueOf(pixel.toLong()).toByteArray().last() != b)
						}

						val newImages = ArrayList<Pair<String, Pixmap>>()
						while (true) {
							var offsetX = 0
							var offsetY = 0

							var subImageWidth = width
							var subImageHeight = height

							if (cropIt) {
								var minY = height
								var maxY = 0
								var minX = width
								var maxX = 0

								for (xx in 0 until width) {
									for (yy in 0 until height) {
										val pixel = image.getPixel(x + xx, y + yy)

										if (!isPixelTransparent(pixel)) {
											if (yy < minY) minY = yy
											if (yy > maxY) maxY = yy

											if (xx < minX) minX = xx
											if (xx > maxX) maxX = xx
										}
									}
								}

								offsetX = minX
								subImageWidth = Math.max(0, maxX - minX) + 1
								offsetY = minY
								subImageHeight = Math.max(0, maxY - minY) + 1
							}

							val newImage = Pixmap(subImageWidth, subImageHeight, Pixmap.Format.RGBA8888)
							newImage.blending = Pixmap.Blending.None
							var isFullyTransparent = true

							for (xx in offsetX until offsetX + subImageWidth) {
								for (yy in offsetY until offsetY + subImageHeight) {
									val pixel = image.getPixel(x + xx, y + yy)
									if (ignoredColor != pixel)
										newImage.drawPixel(xx - offsetX, yy - offsetY, pixel)

									if (isFullyTransparent && !isPixelTransparent(pixel)) {
										isFullyTransparent = false
									}
								}
							}

							if (!ignoreBlankRegions || (!isFullyTransparent || optionalNumberOfFrames != null)) {
								val finalName = name + "_n$i.png"
								log("  created new image #$i: $finalName")
								val parentPath = file.path.replace(file.name, "")
								if (isTilesetMinimal) {
									newImages.add(parentPath to newImage)
								} else {
									PixmapIO.writePNG(FileHandle("$parentPath/$finalName"), newImage)
								}
							} else {
								log("  (!) tile #$i is fully transparent, won't create new image")
							}

							if (!isTilesetMinimal)
								newImage.dispose()

							i++

							if (optionalNumberOfFrames != null && i >= optionalNumberOfFrames!!) {
								log("  (!) reached the specified maximum number " +
										"of frames: #$optionalNumberOfFrames. Stopping now")
								break
							}

							x += width
							if (x + width > image.width) {
								x = 0
								y += height

								if (y + height > image.height) {
									break
								}

								if (replaceHashTag)
									newRowNewImageAnim()
							}
						}

						if (isTilesetMinimal) {
							for ((parentPath, image) in newImages) {

							}
						}

						image.dispose()
						file.delete()
					} else if (fontFile(file)) {
						if (firstFontFile) {
							firstFontFile = false

							val generatedFontsFolder = File(outputFolder, "fonts")
							if (!generatedFontsFolder.exists()) generatedFontsFolder.mkdirs()
							for (f in generatedFontsFolder.listFiles()) {
								if (fontFile(f)) f.delete()
							}
						}

						file.copyTo(File(outputFolder, "fonts/${file.name}"), true)
					}
				}
			}

			TexturePacker.process(imagesOrigin, Paths.get(outputFolder, "atlas").toString(), "atlas")

			log("Finished packing images")
		}
	}
}
