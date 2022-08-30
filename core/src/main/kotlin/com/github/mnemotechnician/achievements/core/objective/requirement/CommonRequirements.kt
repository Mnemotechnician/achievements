package com.github.mnemotechnician.achievements.core.objective.requirement

import com.github.mnemotechnician.achievements.core.misc.MindustryUnit
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective.Requirement
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.BuildingEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents.UnitEvent
import com.github.mnemotechnician.achievements.core.objective.requirement.CustomRequirement.Companion
import com.github.mnemotechnician.mkui.delegates.bundle
import mindustry.gen.Building

/**
 * An inline requirement used in cases when creating a new separate class seems unnecessary.
 *
 * When creating an instance of this class,
 * you must also provide a description bundle entry, whose name is the following string:
 * `requirement.<internalName>.description`.
 * 
 * Instances of this class should be created using the [Companion.invoke] function.
 */
class CustomRequirement(
	internalName: String,
	val predicate: ObjectiveEvent.() -> Boolean
) : Requirement(internalName) {
	override val description by bundle(bundlePrefix)

	override fun isAccepted(event: ObjectiveEvent) = predicate(event)

	companion object {
		/** Constructs a [CustomRequirement], filtering events of type [T]. */
		inline operator fun <reified T : ObjectiveEvent> invoke(
			internalName: String,
			crossinline predicate: T.() -> Boolean
		) = CustomRequirement(internalName) {
			if (this is T) predicate() else true
		}

		/** Constructs a [CustomRequirement], filtering blocks of type [T]. */
		@JvmName("invokeBuilding")
		inline operator fun <reified T : Building> invoke(
			internalName: String,
			crossinline predicate: T.() -> Boolean
		) = CustomRequirement(internalName) {
			if (this is BuildingEvent && building is T) predicate(building as T) else true
		}

		/** Constructs a [CustomRequirement], filtering units of type [T]. */
		@JvmName("invokeUnit")
		inline operator fun <reified T : MindustryUnit> invoke(
			internalName: String,
			crossinline predicate: T.() -> Boolean
		) = CustomRequirement(internalName) {
			if (this is UnitEvent && unit is T) predicate(unit as T) else true
		}
	}
}
