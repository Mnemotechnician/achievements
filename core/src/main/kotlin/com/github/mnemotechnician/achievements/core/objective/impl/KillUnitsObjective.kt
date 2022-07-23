package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.*
import com.github.mnemotechnician.achievements.core.util.lazyBundle
import com.github.mnemotechnician.achievements.core.util.lazySetting
import mindustry.type.UnitType

/**
 * Requires the player to kill [number] enemy units of the specified [kinds].
 */
class KillUnitsObjective(
	val number: Int = 1,
	vararg val kinds: UnitType
) : Objective("kill-units", acceptedEvents) {
	var killed by lazySetting(0) { uniqueName }

	val kindsDescription by lazy { kinds.joinToString(", ") { it.localizedName } }
	override val isFulfilled get() = killed >= number
	override val description by lazyBundle({ bundleName }, { kindsDescription }, { killed }, { number })

	override val progress get() = killed / number.toFloat()

	init {
		require(number >= 0) { "number must be >= 0: $number < 0"}
	}

	constructor(number: Int, unit: UnitType) : this(number, kinds = arrayOf(unit))

	constructor(block: UnitType) : this(1, block)

	override fun handleEvent(event: ObjectiveEvent) {
		if (event is ObjectiveEvents.UnitDestroyedEvent && event.unit.type in kinds) {
			killed++
		}
	}

	companion object {
		val acceptedEvents = setOf(ObjectiveEvents.UnitDestroyedEvent::class.java)
	}
}
