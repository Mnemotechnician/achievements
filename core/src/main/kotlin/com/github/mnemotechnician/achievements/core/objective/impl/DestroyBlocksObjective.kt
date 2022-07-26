package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents
import com.github.mnemotechnician.achievements.core.util.emojiOrName
import com.github.mnemotechnician.mkui.delegates.bundle
import mindustry.Vars
import mindustry.core.Version.number
import mindustry.game.Team
import mindustry.world.Block

/**
 * Requires the player to destroy [number] blocks of the specified [kinds].
 *
 * If [byDeconstruction] is true, the player must deconstruct blocks of their team.
 * Otherwise, they must destroy blocks of an enemy team.
 */
open class DestroyBlocksObjective(
	number: Int,
	val byDeconstruction: Boolean,
	vararg val kinds: Block
) : AbstractCounterObjective(number, "destroy-blocks", if (byDeconstruction) deconstructionEvent else destructionEvent) {
	constructor(byDeconstruction: Boolean, vararg kinds: Block) : this(1, byDeconstruction, *kinds)

	/** By destruction. */
	constructor(number: Int, vararg kinds: Block) : this(number, false, *kinds)

	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		val kindsDescription = kinds.joinToString(", ") { it.emojiOrName() }
		list.add(0) { if (byDeconstruction) deconstruct else destroy }
		list.add(1) { kindsDescription }
	}

	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		if (byDeconstruction) {
			return event is ObjectiveEvents.DeconstructionEvent
				&& event.building.team() == Vars.player.team()
				&& event.building.block in kinds
		} else {
			return event is ObjectiveEvents.BuildingDestroyedEvent
				&& event.building.team() != Vars.player.team()
				&& event.building.team() != Team.derelict
				&& event.building.block in kinds
		}
	}

	companion object {
		val deconstruct by bundle("achievements-core")
		val destroy by bundle("achievements-core")

		val deconstructionEvent = setOf(ObjectiveEvents.DeconstructionEvent::class.java)
		val destructionEvent = setOf(ObjectiveEvents.BuildingDestroyedEvent::class.java)
	}
}
