package com.github.mnemotechnician.achievements.core.objective.requirement

import arc.struct.Seq
import com.github.mnemotechnician.achievements.core.misc.emojiOrName
import com.github.mnemotechnician.achievements.core.misc.int
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective.Requirement
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.BuildingEvent
import com.github.mnemotechnician.mkui.delegates.bundle
import mindustry.type.Item
import mindustry.type.Liquid
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.environment.Floor
import mindustry.world.blocks.production.Drill
import kotlin.math.max

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
				event.building.proximity.any { it?.block == block }
			}
		} else {
			event.building.proximity.any { it.block in blocks }
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

/**
 * Requires the player to put any of the specified liquids in the block.
 * @param inTotal if true, the requirement is fulfilled when the sum of all specified liquids in the block is over [targetCount].
 *      Otherwise, it's fulfilled when the amount of one of these liquids exceeds the specified number.
 */
class ItemRequirement(
	val inTotal: Boolean = false,
	val targetCount: Int,
	vararg val items: Item
) : Requirement("items") {
	val itemNames = items.joinToString(", ") { it.emojiOrName() }
	override val description by bundle(bundlePrefix, targetCount, inTotal.int, itemNames)

	/** Same as the primary constructor. */
	constructor(targetCount: Int, vararg items: Item) : this(false, targetCount, *items)

	override fun isAccepted(event: ObjectiveEvent) = if (event is BuildingEvent) {
		val buildItems = event.building.items

		if (buildItems != null) {
			var count = 0
			if (inTotal) {
				items.forEach { count += buildItems[it] }
			} else {
				items.forEach { count = max(count, buildItems[it] )}
			}
			count >= targetCount
		} else true
	} else true
}


/**
 * Requires the player to put any of the specified liquids in the block.
 * @param inTotal if true, the requirement is fulfilled when the sum of all specified liquids in the block is over [targetAmount].
 *      Otherwise, it's fulfilled when the amount of one of these liquids exceeds the specified number.
 */
class LiquidRequirement(
	val inTotal: Boolean = false,
	val targetAmount: Float,
	vararg val liquids: Liquid
) : Requirement("items") {
	val liquidNames = liquids.joinToString(", ") { it.emojiOrName() }
	override val description by bundle(bundlePrefix, targetAmount, inTotal.int, liquidNames)

	/** Same as the primary constructor. */
	constructor(targetAmount: Float, vararg liquids: Liquid) : this(false, targetAmount, *liquids)

	override fun isAccepted(event: ObjectiveEvent) = if (event is BuildingEvent) {
		val buildLiquids = event.building.liquids

		if (buildLiquids != null) {
			var count = 0f
			if (inTotal) {
				liquids.forEach { count += buildLiquids[it] }
			} else {
				liquids.forEach { count = max(count, buildLiquids[it] )}
			}
			count >= targetAmount
		} else true
	} else true
}
