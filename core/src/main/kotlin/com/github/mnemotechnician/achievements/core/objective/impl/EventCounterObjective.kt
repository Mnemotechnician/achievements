package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.impl.EventCounterObjective.Companion

/**
 * Requires the player to trigger an objective event [targetCount] times.
 *
 * Instances of this class are to be created using the [Companion.invoke] inline function.
 *
 * In addiction to creating an instance of this class,
 * you must also create the following bundle entry:
 * objective.<[name]>.description - the description of this objective.
 */
open class EventCounterObjective<T : ObjectiveEvent>(
	targetCount: Int,
	name: String,
	val type: Class<T>
) : AbstractCounterObjective(targetCount, name, setOf(type)) {
	override fun modifyBundleParams(list: MutableList<() -> Any?>) {}

	override fun receiveEvent(event: ObjectiveEvent): Boolean {
		return type.isInstance(event)
	}

	companion object {
		inline operator fun <reified T : ObjectiveEvent> invoke(
			targetCount: Int,
			name: String,
			filter: Filter<EventCounterObjective<T>, T>
		) = EventCounterObjective(targetCount, name, T::class.java).also {
			it.filter(filter as Filter<Objective, ObjectiveEvent>)
		}
	}
}
