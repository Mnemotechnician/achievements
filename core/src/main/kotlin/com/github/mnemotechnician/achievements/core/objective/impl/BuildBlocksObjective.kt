package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.ObjectiveEvent.*
import com.github.mnemotechnician.achievements.core.util.lazyBundle
import com.github.mnemotechnician.achievements.core.util.lazySetting
import mindustry.world.Block

/**
 * Requires the player to build [number] blocks of the specified [type].
 */
open class BuildBlocksObjective(
	val kind: Block,
	val number: Int = 1
) : Objective("build-blocks", acceptedEvents) {
	var built by lazySetting(0) { uniqueName }

	override val isFulfilled get() = built >= number
	override val description by lazyBundle({ bundleName }, { kind.localizedName }, { built }, { number })

	override val progress get() = built / number.toFloat()

	init {
		require(number >= 0) { "The required number of blocks must be >= 0: $number < 0"}
	}

	override fun handleEvent(event: ObjectiveEvent) {
		if (event is ConstructionEvent && event.build.block == kind) built++
	}

	companion object {
		val acceptedEvents = setOf(ConstructionEvent::class.java)
	}
}
