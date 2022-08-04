package com.github.mnemotechnician.achievements.core.objective

import arc.Core
import com.github.mnemotechnician.achievements.core.StateManager
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.mkui.delegates.dynamicBundle
import kotlin.math.min

/**
 * Represents a simple objective that requires the player to do the same thing [targetCount] times.
 *
 * It's assumed that the bundle entry associated with this objective
 * accepts at least 2 parameters (total count, target count),
 * more can be added by implementing [modifyBundleParams].
 */
abstract class AbstractCounterObjective(
	val targetCount: Int,
	name: String,
	acceptedEvents: Set<Class<out ObjectiveEvent>>
) : Objective(name, acceptedEvents) {
	/** How many times the desired thing has been done. */
	var count by StateManager.state(0) { uniqueName }
	override val isFulfilled get() = count >= targetCount
	override val progress get() = count / targetCount.toFloat()

	/**
	 * A list of additional requirements or null if this objective doesn't have any.
	 * Use [requirement] instead of modifying this list directly.
	 */
	var requirements: ArrayList<Requirement>? = null

	init {
		require(targetCount >= 0) { "targetCount must be >= 0: $targetCount < 0"}
	}

	@Suppress("UNCHECKED_CAST")
	override val description by dynamicBundle(
		bundleName,
		*mutableListOf({ min(targetCount, count) }, { targetCount }).also {
			modifyBundleParams(it as MutableList<() -> Any?>)
		}.toTypedArray()
	)

	override fun handleEvent(event: ObjectiveEvent) {
		if (receiveEvent(event) && isAccepted(event)) {
			count++
		}
	}

	override fun reset() {
		count = 0
	}

	/**
	 * Adds a requirement and initialises [requirements] if necessary.
	 */
	open fun requirement(requirement: Requirement) {
		if (requirements == null) {
			requirements = ArrayList(10)
		}
		requirement.parent = this
		if (isInit) requirement.init()
		requirements!!.add(requirement)
	}

	override fun init() {
		super.init()
		requirements?.forEach { it.init() }
	}

	/**
	 * Should modify the list of bundle parameters, if necessary.
	 * Called during the construction of the class.
	 *
	 * This method is invoked before the class is fully initialised!
	 * It's implementations must not access any fields in a non-lazy way,
	 * as these are very likely to be uninitialised by the invocation time.
	 */
	protected abstract fun modifyBundleParams(list: MutableList<() -> Any?>)

	/**
	 * If this returns true, the counter is incremented.
	 * Events passed to this method may not be in the [acceptedEvents] list, so an instance check is required.
	 */
	abstract fun receiveEvent(event: ObjectiveEvent): Boolean

	/**
	 * Represents an additional requirement added on top of an objective.
	 *
	 * Must have a bundle entry named requirement.<requirement-name>.description,
	 * describing it.
	 *
	 * @param name the internal name, same as that of [Objective].
	 */
	abstract class Requirement(name: String) {
		lateinit var description: String
		lateinit var parent: Objective

		/**
		 * Initialises this requirement.
		 * Must be called from the parent objective before using.
		 * @see Objective.init
		 */
		fun init() {
			if (!::description.isInitialized) {
				description = Core.bundle.get("requirement.$description.name")
			}
		}
	}
}
