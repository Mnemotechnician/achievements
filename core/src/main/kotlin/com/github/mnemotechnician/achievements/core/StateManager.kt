package com.github.mnemotechnician.achievements.core

import arc.Core
import arc.util.Log
import arc.util.Timer
import com.github.mnemotechnician.achievements.core.StateManager.StateEntry
import com.github.mnemotechnician.mkui.delegates.SettingDelegate
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * Stores a list of [state entries][StateEntry], which are normally used as property delegates.
 *
 * Each state entry is stored as a weak reference and is invalidated every 10 seconds.
 * This allows to greatly reduce the performance cost of retrieving a value from the key-value map.
 *
 * If the underlying value can be changed from multiple independent places,
 * [StateEntry] must not be used, as it is only updated once per 10 seconds.
 */
object StateManager {
	// todo right now this class delegates tomCore.settings, later it will be changed
	// /** A map storing all values. */
	// var values = HashMap<String, Any?>(200)

	/** A list of weak references to all allocated state entries. */
	val allocatedStates = ArrayList<WeakReference<StateEntry<*>>>(200)
	/** The interval at which all tasks are invalidated. In seconds. */
	var updateInterval = 10f

	init {
		Timer.schedule({
			invalidateAll()
			// todo remove
			Log.info("invalidating")
		}, updateInterval, updateInterval)
	}

	/** Invalidates all cached value. Called every [updateInterval] seconds. */
	fun invalidateAll() {
		allocatedStates.removeAll { ref ->
			// remove all garbage-collected references and invalidate those that are still there.
			ref.get()?.also {
				it.invalidate()
			} == null
		}
	}

	/** A convenient constructor function for [StateEntry]. */
	fun <T> state(default: T, lazyPrefix: () -> String?) = StateEntry(lazyPrefix, default)

	/**
	 * A caching setting delegate.
	 * Used to reduce performance overhead caused by constant get()ting from [Core.settings].
	 */
	class StateEntry<T>(val lazyPrefix: () -> String?, default: T) : SettingDelegate<T>("", default) {
		/** Holds either the cached value or [NO_VALUE] if there's no value. */
		var cachedValue: Any? = NO_VALUE
		/** If false, there's no cached value right now. */
		val hasCache get() = cachedValue != NO_VALUE

		init {
			allocatedStates.add(WeakReference(this))
		}

		override fun getValue(thisRef: Any?, property: KProperty<*>): T {
			if (hasCache) return cachedValue as T

			return (Core.settings[computeName(property), null] as? T ?: default).also {
				if (it != default) cachedValue = it
			}
		}

		override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
			cachedValue = value
			super.setValue(thisRef, property, value)
		}

		override fun computeName(property: KProperty<*>): String {
			if (prefix.isEmpty()) {
				lazyPrefix().also {
					if (it.isNullOrEmpty()) return "__FALLBACK__"
					prefix = it
				}
			}
			return super.computeName(property)
		}

		/** Invalidates the cached value. */
		fun invalidate() {
			cachedValue = NO_VALUE
		}

		companion object {
			val NO_VALUE = Any()
		}
	}
}
