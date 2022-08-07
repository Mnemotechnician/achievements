package com.github.mnemotechnician.achievements.core.objective.requirement

import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective.Requirement
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.BuildingEvent
import com.github.mnemotechnician.achievements.core.util.emojiOrName
import com.github.mnemotechnician.mkui.delegates.dynamicBundle
import mindustry.world.Block

/**
 * Requires a block in proximity of the block that's being placed or removed.
 * Only makes sense when the objective is a block-related objective.
 *
 * @param allOf if true, all of [blocks] must be present. Otherwise, at least one.
 */
class ProximityRequirement(
	val allOf: Boolean = false,
	vararg val blocks: Block
) : Requirement("proximity") {
	val blockNames = blocks.joinToString(", ") { it.emojiOrName() }
	override val description by dynamicBundle(bundlePrefix, { allOf }, { blockNames })

	/** Same as primary but [allOf] is false. */
	constructor(vararg blocks: Block) : this(false, *blocks)

	override fun isAccepted(event: ObjectiveEvent) = when (event) {
		is BuildingEvent -> if (allOf) {
			blocks.all { block ->
				event.building.proximity.items.any { it.block == block }
			}
		} else {
			event.building.proximity.items.any { it.block in blocks }
		}
		else -> true
	}
}
// todo add more
