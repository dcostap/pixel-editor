package dev.dcostap.utils

import com.badlogic.gdx.utils.JsonValue

/** Created by Darius on 7/16/2018. */
interface Saveable {
	fun save(): JsonValue
	fun load(jsonValue: JsonValue, saveVersion: String)
}