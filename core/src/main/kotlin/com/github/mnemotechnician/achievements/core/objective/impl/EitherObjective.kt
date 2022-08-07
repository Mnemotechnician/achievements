package com.github.mnemotechnician.achievements.core.objective.impl

import arc.scene.ui.layout.Table
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.mkui.delegates.bundle
import com.github.mnemotechnician.mkui.extensions.dsl.addTable

/**
 * A wrapper objectives that requires the player to complete
 * at least one of the wrapped objectives.
 */
class EitherObjective(
	vararg val objectives: Objective
) : Objective("either") {
	override val description by bundle(bundleName)
	override val isFulfilled = objectives.any { it.isFulfilled }

	init {
		require(objectives.isNotEmpty()) { "The array of objectives of EitherObjective must not be empty." }
	}

	override fun reset() {
		objectives.forEach { it.reset() }
	}

	override fun handleEvent(event: ObjectiveEvent) {
		objectives.forEach { it.handleEvent(event) }
	}

	override fun display(target: Table) {
		super.display(target)
		// display variants
		target.row().addTable {
			objectives.forEach {
				addTable {
					it.display(this)
				}.padLeft(3f)
			}
		}.colspan(2)
	}
}