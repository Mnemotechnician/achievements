package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent

/**
 * Requires the player to trigger an objective event [targetCount] times.
 *
 * Instances of this class are to be created using the [Companion.invoke] inline function.
 *
 * In addiction to creating an instance of this class,
 * you must also create the following bundle entries:
 * * objective.<[name]>.name - the name of this objective.
 * * objective.<[name]>.description - the description. This one should accept two parameters:
 *     * `{0}` - The amount of times it's been scored.
 *     * `{1}` - [targetCount], aka how many times it has to be scored.
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
		inline fun <reified T : ObjectiveEvent> invoke(
			targetCount: Int,
			name: String,
			filter: Filter<EventCounterObjective<T>, T>
		) = EventCounterObjective(targetCount, name, T::class.java).also {
			it.filter(filter as Filter<Objective, ObjectiveEvent>)
		}
	}
}
