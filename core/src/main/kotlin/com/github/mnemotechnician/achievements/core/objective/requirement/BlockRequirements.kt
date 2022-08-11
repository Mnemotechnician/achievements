package com.github.mnemotechnician.achievements.core.objective.requirement

import arc.struct.Seq
import com.github.mnemotechnician.achievements.core.misc.emojiOrName
import com.github.mnemotechnician.achievements.core.misc.int
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective.Requirement
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.BuildingEvent
import com.github.mnemotechnician.mkui.delegates.bundle
import mindustry.type.Item
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.production.Drill

/**
 * Requires a block in proximity of the block that's being placed or removed.
 * Only makes sense when the objective is a block-related objective.
 *
 * @param allOf if true, all of [blocks] must be present. Otherwise, at least one.
 */
class ProximityRequirement(
	val allOf: Boolean = true,
	vararg val blocks: Block
) : Requirement("proximity") {
	val blockNames = blocks.joinToString(", ") { it.emojiOrName() }
	override val description by bundle(bundlePrefix, allOf.int, blockNames)

	/** Same as the primary constructor. */
	constructor(vararg blocks: Block) : this(true, *blocks)

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

/**
 * Requires the player to build blocks on a specific overlay or floor.
 *
 * @param overlay if true, the floor must be an overlay floor.
 * @param allOf if true, the block must stand on each of the specified floors/overlays. Otherwise, at least on one.
 */
class FloorRequirement(
	val overlay: Boolean = false,
	val allOf: Boolean = true,
	vararg val floors: Floor
) : Requirement("floor") {
	val blockNames = floors.joinToString(", ") { it.emojiOrName() }
	override val description by bundle(bundlePrefix, allOf.int, blockNames)

	/** Same as the primary constructor. */
	constructor(overlay: Boolean = false, vararg floors: Floor) : this(overlay, true, *floors)

	override fun isAccepted(event: ObjectiveEvent) = if (event is BuildingEvent) {
		if (allOf) {
			val occupied = event.building.tile.getLinkedTiles(tmpSeq)

			floors.all {  floor ->
				occupied.any {
					(if (overlay) it.overlay() else it.floor()) == floor
				}
			}
		} else {
			var result = false
			event.building.tile.getLinkedTiles {
				val floor = if (overlay) it.overlay() else it.floor()
				if (floor in floors) result = true
			}
			result
		}
	} else true

	companion object {
		private val tmpSeq = Seq<Tile>()
	}
}

/**
 * A drill-specific requirement, which requires the player to make the drills mine a specific item.
 * Has no effect on non-drill blocks.
 */
class MiningRequirement(val minedItem: Item) : Requirement("mining") {
	override val description by bundle(bundlePrefix, minedItem.emojiOrName())

	override fun isAccepted(event: ObjectiveEvent) = if (event is BuildingEvent) {
		val build = event.building
		if (build is Drill.DrillBuild) {
			build.dominantItem == minedItem
		} else true
	} else true
}
