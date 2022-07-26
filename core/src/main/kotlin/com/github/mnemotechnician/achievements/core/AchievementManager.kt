package com.github.mnemotechnician.achievements.core

import arc.Events
import arc.util.Log
import com.github.mnemotechnician.achievements.core.misc.optForEach
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import mindustry.Vars
import mindustry.game.EventType

/**
 * Manages all registered achievements.
 *
 * Ignores any events if the `infiniteResources` game rule is true.
 */
object AchievementManager {
	/** All root achievements. Do not modify. */
	val achievements = ArrayList<Achievement>(10)
	/** All registered achievements, including the children. */
	val allAchievements = ArrayList<Achievement>(50)
	/** Objectives with updates = true, updated on every frame. */
	val updatingObjectives = ArrayList<Objective>(100)

	/**
	 * All kinds of events accepted by [fireEvent].
	 * When an objective is created, it adds an entry to this set
	 * by calling [addEvent].
	 */
	val acceptedEvents = HashSet<Class<out ObjectiveEvent>>()

	init {
		Events.run(EventType.Trigger.update) {
			updatingObjectives.forEach { 
				if (it.parent.isUnlocked) it.update() 
			}
		}
	}

	/**
	 * Registers and initialises an achievement.
	 * Adds it to [achievements] if it's doesn't have a parent.
	 */
	fun register(achievement: Achievement) {
		achievement.init()
		allAchievements.add(achievement)

		if (achievement.parent == null) {
			achievements.add(achievement)
		}
	}

	/** Adds this objective to [updatingObjectives]. */
	fun addUpdatingObjective(objective: Objective) {
		updatingObjectives.add(objective)
	}

	/**
	 * Notifies all achievements that this event has occurred,
	 * or does nothing if no objectives have reported that they listen for this event class.
	 */
	fun fireEvent(event: ObjectiveEvent) {
		if (Vars.headless || event::class.java !in acceptedEvents) return

		achievements.optForEach {
			it.handleEvent(event)
		}
	}

	/**
	 * Notifies the that this event type should be accepted.
	 * Initialises this objective event type if it hasn't been yet.
	 */
	fun addEvent(type: Class<out ObjectiveEvent>) {
		if (acceptedEvents.add(type)) {
			// how optimal is this?
			try {
				val cls = ObjectiveEvent.Listener::class.java

				(type.declaredClasses.find {
					cls.isAssignableFrom(it)
				} ?: throw NullPointerException("The event class doesn't declare a listener subclass.")).getDeclaredConstructor().newInstance()
			} catch (e: Exception) {
				Log.warn("Couldn't init a listener for the $type class: $e")
			}
		}
	}

	/** Finds a registered achievement by its name. */
	fun getForName(name: String, ignoreCase: Boolean = true): Achievement? {
		return allAchievements.find { it.name.equals(name, ignoreCase) }
	}

	/**
	 * Resets everything. For debug purposes only.
	 */
	fun hardReset() {
		allAchievements.forEach {
			it.isCompleted = false
			it.objectives.forEach {
				it.reset()
			}
		}

		achievements.forEach {
			it.update(false)
		}
	}

	/** Updates the completion state of all achievements. */
	fun updateAll(silent: Boolean) = allAchievements.forEach {
		it.update(silent)
	}

	/** Counts completed achievements. */
	fun countCompleted(): Int {
		return allAchievements.count { it.isCompleted }
	}
}
