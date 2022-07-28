package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents
import com.github.mnemotechnician.achievements.core.util.emojiOrName
import mindustry.Vars
import mindustry.core.Version.number
import mindustry.type.UnitType

/**
 * Requires the player to kill [number] enemy units of the specified [kinds].
 */
open class BuildUnitsObjective(
	number: Int = 1,
	vararg val kinds: UnitType
) : AbstractCounterObjective(number, "building-units", acceptedEvents) {
	constructor(unit: UnitType) : this(1, unit)

	val kindsDescription by lazy { kinds.joinToString(", ") { it.emojiOrName() } }

	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		list.add(0) { kindsDescription }
	}

	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		return event is ObjectiveEvents.UnitConstructionEvent
			&& event.unit.team() == Vars.player.team()
			&& event.unit.type in kinds
	}

	companion object {
		val acceptedEvents = setOf(ObjectiveEvents.UnitConstructionEvent::class.java)
	}
}


