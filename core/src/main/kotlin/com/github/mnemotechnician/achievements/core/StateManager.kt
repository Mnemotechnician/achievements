package com.github.mnemotechnician.achievements.core

import arc.Core
import arc.Events
import arc.files.Fi
import arc.util.Log
import arc.util.Timer
import arc.util.io.Reads
import arc.util.io.Writes
import com.github.mnemotechnician.achievements.core.Achievement.AchievementUnlockEvent
import com.github.mnemotechnician.achievements.core.StateManager.StateEntry
import com.github.mnemotechnician.mkui.delegates.SettingDelegate
import mindustry.Vars
import mindustry.core.GameState.*
import mindustry.game.EventType.SaveLoadEvent
import mindustry.game.EventType.StateChangeEvent
import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * Stores a map of string keys and values, similarly to [Settings].
 * The map is regularly saved to and loaded from files.
 *
 * Additionally, it stores a list of [state entries][StateEntry], which are normally used as property delegates.
 *
 * Each state entry is stored as a weak reference and is invalidated every 10 seconds.
 * This allows to greatly reduce the performance cost of retrieving a value from the key-value map.
 *
 * If the underlying value can be used in multiple places with multiple different delegates,
 * [StateEntry] must not be used, as it is only updated once per 10 seconds.
 */
object StateManager {
	/** A map storing all values. */
	var values = HashMap<String, Any?>(200)
		private set
	/** All i/o types this manager supports. Do not modify directly. */
	val ioTypes = ArrayList<IOType<*>>()
	val ioTypeMap = HashMap<Class<*>, IOType<*>>()

	/** A list of weak references to all allocated state entries. */
	val allocatedStates = ArrayList<WeakReference<StateEntry<*>>>(200)
	/** The interval at which all state entries are invalidated. In seconds. */
	var updateInterval = 10f

	/** Used to save the state before loading a new one. */
	var lastStateFile: Fi? = null
		private set
	/** The header with which all save files must begin. */
	const val header = "ACHIEVEMENTS"
	val headerBytes = header.toByteArray()
	const val backupSuffix = "-backup"
	/**
	 * Three bytes delimiting values in a state save file.
	 * Used to prevent deserialization errors when some values can't be deserialized.
	 */
	val valueDelimiterBytes = byteArrayOf(-128, 0, 127)

	init {
		Timer.schedule({
			invalidateAll()
		}, updateInterval, updateInterval);

		Events.on(AchievementUnlockEvent::class.java) {
			autoSaveState()
		}
		Events.on(SaveLoadEvent::class.java) {
			lastStateFile?.let { saveState(it) }
			Core.app.post { autoLoadState() }
		}
		Events.on(StateChangeEvent::class.java) {
			lastStateFile?.let { saveState(it) }
			if (it.to == State.menu) autoLoadState()
		}

		// integers
		addType({ b() }, { b(it.toInt()) })
		addType({ s() }, { s(it.toInt()) })
		addType({ i() }, { i(it) })
		// decimals
		addType({ f() }, { f(it) })
		addType({ d() }, { d(it) })
		// other
		addType<Unit>({}, {}) // i don't know why this happens to be there and at this point i'm too afraid to ask
		addType({ bool() }, { bool(it) })
		addType({ str() }, { str(it) })
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

	/**
	 * Saves the current state to the specified file and, if [file] exists, creates a backup.
	 * @throws IllegalStateException if any of the values can't be serialized.
	 */
	fun saveState(file: Fi) {
		if (file.exists() && !file.isDirectory && !isBackupFile(file) && isSaveFile(file)) {
			val backup = file.sibling(file.name() + backupSuffix)
			backup.removeIfDir()

			file.copyTo(backup)
		}

		file.writes(false).use { writes ->
			// header
			writes.b(headerBytes)
			// class list
			val classes = ioTypes.map { it.type }.distinct()
			writes.i(classes.size)
			classes.forEach {
				writes.str(it.canonicalName)
			}
			ioTypes.forEach {
				it.ordinal = classes.indexOf(it.type)
			}

			// actual entries
			writes.i(values.size)
			values.forEach { (key, value) ->
				writes.str(key)
				if (value == null) {
					writes.i(-1)
				} else {
					val serializer = ioTypeMap.getOrDefault(value::class.java, null)
						?: throw IllegalStateException("No serializer for key \"$key\" (type ${value::class.java})")

					writes.i(serializer.ordinal)
					(serializer.write as Writes.(Any?) -> Unit)(writes, value)

					valueDelimiterBytes.forEach { writes.b(it.toInt()) }
				}
			}
		}
	}

	/**
	 * Loads a state save file, overriding the current state upon success.
	 *
	 * @param file can be null, in which case the state will be simply reset.
	 *
	 * @throws IllegalArgumentException if the file is not a valid state save file.
	 * @throws RuntimeException if the save file is corrupted
	 */
	fun loadState(file: Fi?) {
		if (file == null) {
			lastStateFile = null
			values.clear()
			invalidateAll()
			return
		}

		file.reads().use { reads ->
			headerBytes.forEach {
				if (reads.b() != it) throw IllegalArgumentException("File ${file.absolutePath()} is not a valid state save file!")
			}

			// class list
			val classCount = reads.i()
			val deserializers = List(classCount) {
				val name = reads.str()

				try {
					ioTypeMap[Class.forName(name)]
				} catch (e: Exception) {
					Log.warn("The save file requires a deserializer for '$name', but it wasn't found. Ignoring entries with this type.")
					null
				}
			}

			// entries
			val totalEntries = reads.i()
			val newValues = HashMap<String, Any?>(totalEntries)

			(0 until totalEntries).forEach {
				val key = reads.str()
				val type = reads.i()

				val value = if (type == -1) {
					null
				} else {
					val deserializer = deserializers.getOrNull(type)

					deserializer?.let {
						it.read(reads)
					}.also {
						// skip 3 delimiter bytes
						while (
							reads.b() != valueDelimiterBytes[0]
							|| reads.b() != valueDelimiterBytes[1]
							|| reads.b() != valueDelimiterBytes[2]
						) continue
					} ?: return@forEach
				}

				newValues[key] = value
			}

			values = newValues
			invalidateAll()

			lastStateFile = file
		}

		AchievementManager.allAchievements.forEach {
			it.update(true)
		}
	}

	/**
	 * Automatically detects which kind of state should be loaded right now.
	 * This loads different states for campaign, custom games, etc.
	 *
	 * Unless an i/o error occurs, this method never throws an exception.
	 */
	fun autoLoadState() {
		val root = Vars.dataDirectory.child("saves").child("achievements")

		if (root.exists() && !root.isDirectory) root.delete()
		if (!root.exists()) root.mkdirs()

		var save = root.child(determineSaveName() + ".state")
		val backup = save.sibling(save.name() + backupSuffix)

		if (!save.exists()) save = backup
		save.removeIfDir()

		try {
			loadState(if (!save.exists()) null else save)
		} catch (e: Exception) {
			Log.err("Could not load state save file: ${save.absolutePath()}", e)
			// try loading the backup
			if (backup != save && backup.exists() && !backup.removeIfDir()) try {
				loadState(backup)
			} catch (e: Exception) {
				Log.err("Could not save a backup file either", e)
			}
		}
	}

	/** Saves the current state automatically. */
	fun autoSaveState() {	
		val root = Vars.dataDirectory.child("saves").child("achievements")

		if (root.exists() && !root.isDirectory) root.delete()
		if (!root.exists()) root.mkdirs()

		try {
			saveState(root.child(determineSaveName() + ".state").also {
				it.removeIfDir()
			})
		} catch (e: Exception) {
			Log.err("Failed to save the current state", e)
		}
	}

	fun determineSaveName() = when {
		Vars.state.isCampaign -> "campaign"
		Vars.net.client() -> "multiplayer"
		!Vars.state.`is`(State.menu) -> "custom-${Vars.state.map.name()}-${Vars.state.map.author()}"
		else -> "campaign"
	}

	/** Returns true if the file is a state save file. */
	fun isSaveFile(file: Fi): Boolean {
		return try {
			String(file.reads().use {
				it.b(headerBytes.size)
			}) == header
		} catch (e: Exception) {
			false
		}
	}

	/** Returns true if the file **might** be a backup file. */
	fun isBackupFile(file: Fi) = file.name().endsWith(backupSuffix)

	operator fun <T> get(key: String) = values[key] as T

	fun <T> getOrNull(key: String) = values.getOrDefault(key, null) as? T

	fun <T> getOrDefault(key: String, default: T): T {
		return getOrNull<T>(key) ?: default
	}

	operator fun set(key: String, value: Any?) {
		values[key] = value
	}

	/**
	 * Adds support for serializing and deserializing the providen class.
	 * This method must be called before loading any state.
	 */
	fun <T> addType(type: Class<T>, read: Reads.() -> T, write: Writes.(T) -> Unit) {
		IOType(type, read, write).let {
			ioTypes.add(it)
			ioTypeMap[type] = it
		}
	}

	/** @see addType */
	inline fun <reified T> addType(noinline read: Reads.() -> T, noinline write: Writes.(T) -> Unit) {
		addType(T::class.java, read, write)
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

			return (values.getOrDefault(computeName(property), null) as? T ?: default).also {
				if (it != default) cachedValue = it
			}
		}

		override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
			cachedValue = value
			values[computeName(property)] = value
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

	/** Represents a pair of functions that can serialize and deserialize a type. */
	data class IOType<T>(
		val type: Class<T>,
		val read: Reads.() -> T,
		val write: Writes.(T) -> Unit
	) {
		/** Used by i/o functions to store the index of this i/o type. */
		var ordinal = -1
	}
}

private fun Fi.removeIfDir(): Boolean {
	return if (exists() && isDirectory) deleteDirectory() else false
}
