package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.misc.emojiOrName
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.ConstructionEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.DeconstructionEvent
import com.github.mnemotechnician.achievements.core.world.TileIndexer
import mindustry.Vars
import mindustry.core.Version.number
import mindustry.world.Block
import mindustry.world.blocks.ConstructBlock.ConstructBuild

/**
 * Requires the player to building [number] blocks of the specified [kinds].
 *
 * Addictionally, when the player deconstructs a block specified in [kinds], the counter is decremented.
 */
open class BuildBlocksObjective(
	number: Int = 1,
	vararg val kinds: Block
) : AbstractCounterObjective(number, "build-blocks", acceptedEvents) {
	val kindsDescription by lazy { kinds.joinToString(", ") { it.emojiOrName() } }

	init {
		kinds.forEach { TileIndexer.indexBlock(it) }
	}

	constructor(vararg blocks: Block) : this(1, *blocks)

	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		list.add(0) { kindsDescription }
	}

	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		if (event is ConstructionEvent) {
			return event.building.team() == Vars.player.team() && event.building.block in kinds
		} else if (event is DeconstructionEvent) {
			val build = (event.building as? ConstructBuild)?.let { TileIndexer.getDeconstructedBuild(it) } ?: event.building
			build.block in kinds
		}
		return false
	}

	companion object {
		val acceptedEvents = setOf(ConstructionEvent::class.java, DeconstructionEvent::class.java)
	}
}
