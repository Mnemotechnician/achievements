package com.github.mnemotechnician.achievements.core.objective

import arc.Core
import arc.scene.ui.layout.Table
import arc.util.Align.left
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.mkui.extensions.dsl.addLabel
import mindustry.graphics.Pal

/**
 * Represents an objective of an [Achievement].
 * Like achievements, has an [internal name][name], a [displayed name][displayName] and a [description].
 *
 * The display name of this objective is taken from a bundle entry
 * whose name is `objective.<[name]>.name.
 *
 * @param name the name of this objective. Must be shared between all instances of the same subclass.
 * @param acceptedEvents the event types this objective accepts.
 */
abstract class Objective(
	val name: String,
	val acceptedEvents: Set<Class<out ObjectiveEvent>> = NO_EVENTS
) {
	/**
	 * A unique name of this objective, which should be used to store the state.
	 * This property depends on the name of the parent achievement and the position of the objective.
	 *
	 * Returns null if the parent property hasn't been initialised yet.
	 */
	var uniqueName: String = ""
		get() {
			if (field.isNotEmpty() || !::parent.isInitialized) return field
			field = "$bundleName.${parent.name}.${parent.objectives.indexOf(this)}"
			return field
		}
		private set

	/** The name this objective should be referred to in bundles. */
	val bundleName by lazy { "objective.$name" }
	/** The name of this objective, displayed in-game. Currently unused. */
	lateinit var displayName: String
	/** The description of this objective displayed in-game. */
	abstract val description: String

	/** Whether this objective has been fulfilled. */
	abstract val isFulfilled: Boolean
	/**
	 * The achievement this objective belongs to.
	 */
	lateinit var parent: Achievement

	/** Progress of this achievement. By default, returns 1 or 0 based on whether it's completed. */
	open val progress get() = if (isFulfilled) 1f else 0f
	/** Objective filters. If one of these returns false, the event is to be ignored. */
	val filters = ArrayList<Filter<Objective, ObjectiveEvent>>(3)

	/** Whether this objective has been initialised yet. */
	var isInit = false
		protected set

	/**
	 * Initialises this achievement.
	 * Called by the parent achievement during its initialisation or immediately after being added.
	 */
	open fun init() {
		if (!::displayName.isInitialized) {
			displayName = Core.bundle.get("objective.$name.name")
		}

		acceptedEvents.forEach { AchievementManager.addEvent(it) }
		isInit = true
	}

	/** Resets this objective to the uncompleted state. */
	abstract fun reset()

	/**
	 * Notifies this objective that an event has occurred, possibly making it progress.
	 * If one of the [filters] returns false, the event is ignored.
	 *
	 * Implementations of this method should pass the event to [isAccepted]
	 * after performing own checks and ignore the event if the method returns false.
	 *
	 * If the event is accepted, it must be mentioned in [acceptedEvents].
	 */
	abstract fun handleEvent(event: ObjectiveEvent)

	/**
	 * Checks whether this event passes all [filters].
	 * If this method returns false, this objective should ignore the providen event.
	 */
	protected open fun isAccepted(event: ObjectiveEvent) = filters.all {
		with(it) { this@Objective(event) }
	}

	/**
	 * Adds an objective filter. The providen function is invoked whenever this objective
	 * receives an [ObjectiveEvent]. If it returns false, the event is ignored.
	 *
	 * @return this objective for chaining.
	 */
	open fun filter(filter: Filter<Objective, ObjectiveEvent>) = this.also {
		filters.add(filter)
	}

	/** Displays this objective on a table. */
	open fun display(target: Table) {
		target.addLabel({ if (isFulfilled) "[green][X] " else "[gray][ ] " }, wrap = false, align = left)
		target.addLabel({ description }, wrap = true, align = left).color(Pal.lightishGray).growX()
	}

	override fun toString() = "${super.toString().substringBefore('@')}(name=$name, isFulfilled=$isFulfilled)"

	companion object {
		/** An empty set of objective events. */
		val NO_EVENTS: Set<Class<out ObjectiveEvent>> = setOf()
	}

	/** `Filter<O : Objective, E : ObjectiveEvent> = O.(E) -> Boolean` */
	fun interface Filter<in O : Objective, in E : ObjectiveEvent> {
		operator fun O.invoke(event: E): Boolean
	}
}
