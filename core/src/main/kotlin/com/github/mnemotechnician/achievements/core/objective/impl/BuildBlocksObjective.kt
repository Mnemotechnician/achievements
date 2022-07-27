package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.ConstructionEvent
import com.github.mnemotechnician.achievements.core.util.emojiOrName
import mindustry.Vars
import mindustry.core.Version.number
import mindustry.world.Block

/**
 * Requires the player to build [number] blocks of the specified [kinds].
 */
open class BuildBlocksObjective(
	number: Int = 1,
	vararg val kinds: Block
) : AbstractCounterObjective(number, "build-blocks", acceptedEvents) {
	constructor(block: Block) : this(1, block)

	val kindsDescription by lazy { kinds.joinToString(", ") { it.emojiOrName() } }

	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		list.add(0) { kindsDescription }
	}

	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		return event is ConstructionEvent
			&& event.build.team() == Vars.player.team()
			&& event.build.block in kinds
	}

	companion object {
		val acceptedEvents = setOf(ConstructionEvent::class.java)
	}
}
