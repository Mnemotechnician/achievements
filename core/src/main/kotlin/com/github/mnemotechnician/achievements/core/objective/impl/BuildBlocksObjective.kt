package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.ObjectiveEvents.ConstructionEvent
import com.github.mnemotechnician.achievements.core.util.lazyBundle
import com.github.mnemotechnician.achievements.core.util.lazySetting
import mindustry.world.Block

/**
 * Requires the player to build [number] blocks of the specified [kinds].
 */
open class BuildBlocksObjective(
	val number: Int = 1,
	vararg val kinds: Block
) : Objective("build-blocks", acceptedEvents) {
	var built by lazySetting(0) { uniqueName }

	val kindsDescription by lazy { kinds.joinToString(", ") { it.localizedName } }
	override val isFulfilled get() = built >= number
	override val description by lazyBundle({ bundleName }, { kindsDescription }, { built }, { number })

	override val progress get() = built / number.toFloat()

	init {
		require(number >= 0) { "number must be >= 0: $number < 0"}
	}

	constructor(number: Int, block: Block) : this(number, kinds = arrayOf(block))

	constructor(block: Block) : this(1, block)

	override fun handleEvent(event: ObjectiveEvent) {
		if (event is ConstructionEvent && event.build.block in kinds) {
			built++
		}
	}

	companion object {
		val acceptedEvents = setOf(ConstructionEvent::class.java)
	}
}
