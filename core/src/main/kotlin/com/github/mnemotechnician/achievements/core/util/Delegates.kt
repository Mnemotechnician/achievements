package com.github.mnemotechnician.achievements.core.util

import arc.Core
import arc.util.Strings
import com.github.mnemotechnician.mkui.delegates.BundleDelegate
import com.github.mnemotechnician.mkui.delegates.SettingDelegate
import kotlin.reflect.KProperty

// TODO make this an mkui feature

/**
 * Creates a setting delegate similar to [SettingDelegate] but with a lazily computed name.
 */
fun <T> lazySetting(default: T, prefix: () -> String) = LazySettingDelegate(prefix, default)

/**
 * Creates a bundle delegate similar to [BundleDelegate] but with a lazily computed name.
 */
fun lazyBundle(prefix: () -> String, vararg substitutions: () -> Any?) = LazyBundleDelegate(prefix, substitutions)

/**
 * Creates a bundle delegate similar to [BundleDelegate] but with a lazily computed name.
 */
fun lazyBundle(prefix: () -> String) = LazyBundleDelegate(prefix, null)

class LazySettingDelegate<T>(val prefix: () -> String, val default: T) {
	private var cachedName: String? = null

	operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return Core.settings.get(computeName(property), default) as? T ?: default
	}

	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		Core.settings.put(computeName(property), value)
	}

	private fun computeName(property: KProperty<*>) = cachedName ?: run {
		val prefix = prefix()
		val sep = if (prefix.endsWith(".") || prefix.endsWith("-")) "" else "."
		"$prefix$sep${Strings.camelToKebab(property.name)}".also { cachedName = it }
	}
}

/**
 * Lazy bundle delegate. Caches the result.
 * Only updates when the output of [substitutions] changes.
 */
class LazyBundleDelegate(
	val prefix: () -> String,
	val substitutions: Array<out () -> Any?>?
) {
	private var cachedName: String? = null
	private var cachedResult: String? = null
	private val tempArray = if (substitutions != null) Array<Any?>(substitutions.size) { null } else null

	var isModified = false

	operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
		val tempArray = tempArray

		return if (substitutions != null && substitutions.isNotEmpty() && tempArray != null) {
			repeat(substitutions.size) {
				val sub = substitutions[it]()
				if (sub != tempArray[it]) {
					tempArray[it] = sub
					isModified = true
				}
			}

			if (isModified || cachedResult == null) {
				Core.bundle.format(computeName(property), *tempArray).also {
					cachedResult = it
				}
			} else {
				cachedResult!!
			}
		} else {
			Core.bundle[computeName(property)]
		}
	}

	private fun computeName(property: KProperty<*>) = cachedName ?: run {
		val prefix = prefix()
		val sep = if (prefix.endsWith(".") || prefix.endsWith("-")) "" else "."
		"$prefix$sep${Strings.camelToKebab(property.name)}".also { cachedName = it }
	}
}
