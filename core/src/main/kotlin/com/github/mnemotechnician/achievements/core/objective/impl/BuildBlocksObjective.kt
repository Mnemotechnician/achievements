package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.misc.emojiOrName
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.*
import mindustry.Vars
import mindustry.core.Version.number
import mindustry.world.Block
import mindustry.world.blocks.ConstructBlock.ConstructBuild
import mindustry.world.blocks.environment.Floor

/**
 * Requires the player to building [number] blocks of the specified [kinds].
 */
open class BuildBlocksObjective(
	number: Int = 1,
	vararg val kinds: Block
) : AbstractCounterObjective(number, "build-blocks", acceptedEvents) {
	constructor(vararg blocks: Block) : this(1, *blocks)

	val kindsDescription by lazy { kinds.joinToString(", ") { it.emojiOrName() } }

	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		list.add(0) { kindsDescription }
	}

	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		if (event is ConstructionEvent) {
			return event.building.team() == Vars.player.team() && event.building.block in kinds
		} else if (event is DeconstructionEvent) {
			val block: Block = (event.building as? ConstructBuild)?.let {
				it.previous ?: it.prevBuild.takeIf { !it.isEmpty }?.first()?.block
			} ?: event.building.block

			// decrement if the target building was deconstructed...
			// since we can't acquire a Building, which filters and requirements rely on, isAccepted() is not called.
			if (event.building.team() == Vars.player.team() && block in kinds) {
				count--
			}
		}
		return false
	}

	/** Adds a placement floor requirement. */
	fun onFloor(floor: Floor) = this.also {
		filter { (it as? BuildingEvent)?.building?.floor() == floor }
	}

	/** Adds a placement overlay requirement. */
	fun onOverlay(overlay: Floor) = this.also {
		filter { (it as? BuildingEvent)?.building?.tile?.overlay() == overlay }
	}

	companion object {
		val acceptedEvents = setOf(ConstructionEvent::class.java, DeconstructionEvent::class.java)
	}
}
