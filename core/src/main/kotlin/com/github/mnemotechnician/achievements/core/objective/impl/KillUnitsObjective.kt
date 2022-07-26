package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.UnitDestroyedEvent
import com.github.mnemotechnician.achievements.core.util.emojiOrName
import mindustry.Vars
import mindustry.core.Version.number
import mindustry.game.Team
import mindustry.type.UnitType

/**
 * Requires the player to kill [number] enemy units of the specified [kinds].
 */
class KillUnitsObjective(
	number: Int = 1,
	vararg val kinds: UnitType
) : AbstractCounterObjective(number, "kill-units", acceptedEvents) {
	constructor(number: Int, unit: UnitType) : this(number, kinds = arrayOf(unit))

	constructor(unit: UnitType) : this(1, unit)

	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		val kindsDescription = kinds.joinToString(", ") { it.emojiOrName() }
		list.add(0) { kindsDescription }
	}

	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		return event is ObjectiveEvents.UnitDestroyedEvent
			&& event.unit.team() != Vars.player.team()
			&& event.unit.team != Team.derelict
			&& event.unit.type in kinds
	}

	companion object {
		val acceptedEvents = setOf(UnitDestroyedEvent::class.java)
	}
}
