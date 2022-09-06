package com.github.mnemotechnician.achievements.core.objective.impl

import arc.scene.ui.layout.Table
import com.github.mnemotechnician.achievements.core.misc.optForEach
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.mkui.delegates.bundle
import com.github.mnemotechnician.mkui.extensions.dsl.addTable

/**
 * A wrapper objectives that requires the player to complete
 * at least one of the wrapped objectives.
 */
open class EitherObjective(
	vararg val objectives: Objective
) : Objective("either") {
	override val description by bundle(bundleName)
	override val isFulfilled get() = objectives.any { it.isFulfilled }
	override val progress get() = objectives.maxOf { it.progress }

	init {
		require(objectives.isNotEmpty()) { "The array of objectives of EitherObjective must not be empty." }
	}

	override fun init() {
		super.init()

		objectives.forEach { 
			it.parent = parent // the parent isn't aware
			it.init()
		}
	}

	override fun reset() {
		objectives.optForEach { it.reset() }
	}

	override fun handleEvent(event: ObjectiveEvent) {
		objectives.forEach { it.handleEvent(event) }
	}

	override fun update() {
		objectives.forEach { it.update() }
	}

	override fun display(target: Table) {
		super.display(target)
		// display variants
		target.row().addTable {
			left()
			objectives.forEach {
				addTable {
					left()
					it.display(this)
				}.padLeft(10f).growX().row()
			}
		}.growX()
	}
}
