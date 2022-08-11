package com.github.mnemotechnician.achievements.core.objective

import arc.graphics.Color
import arc.scene.ui.layout.Table
import arc.util.Align.left
import com.github.mnemotechnician.achievements.core.StateManager
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.mkui.delegates.dynamicBundle
import com.github.mnemotechnician.mkui.extensions.dsl.addLabel
import com.github.mnemotechnician.mkui.extensions.dsl.addTable
import mindustry.graphics.Pal
import kotlin.math.min

/**
 * Represents a simple objective that requires the player to do the same thing [targetCount] times.
 *
 * It's assumed that the bundle entry associated with this objective
 * accepts at least 2 parameters (total targetCount, target targetCount),
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
	 *
	 * This list must be inflated before initialising this objective,
	 * doing elsewise may result in undefined behaviour.
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

	override fun display(target: Table) {
		target.apply {
			addTable {
				super.display(this)
			}.growX().row()

			requirements?.forEach {
				addTable {
					it.display(this)
				}.padLeft(8f).growX().row()
			}
		}
	}

	/**
	 * Adds a requirement and initialises [requirements] if necessary.
	 *
	 * @throws IllegalStateException if called after initialising this objevtive.
	 */
	open fun requirement(requirement: Requirement) {
		if (requirements == null) {
			requirements = ArrayList(10)
		}
		if (isInit) throw IllegalStateException("Requirements must be added before initialising the objective!")
		requirement.parent = this
		requirements!!.add(requirement)
	}

	override fun init() {
		super.init()
		requirements?.forEach { it.init() }
	}

	override fun isAccepted(event: ObjectiveEvent): Boolean {
		return super.isAccepted(event) && (requirements?.all { it.isAccepted(event) } ?: true)
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
	abstract class Requirement(val name: String) {
		abstract val description: String
		/** Bundle entries referencing this requirement must begin with this string, */
		val bundlePrefix get() = "requirement.$name"

		lateinit var parent: Objective

		/**
		 * Initialises this requirement.
		 * Must be called from the parent objective before using.
		 * @see Objective.init
		 */
		open fun init() {
			if (!::parent.isInitialized) {
				throw IllegalStateException("Requirement.parent must be initialised before calling init().")
			}
		}

		/** Same as [Objective.display]. */
		open fun display(target: Table) {
			target.addLabel("> ").color(Pal.lightishGray)
			target.addLabel({ description }, align = left).color(Color.gray).growX()
		}

		/**
		 * Returns true if this event passes the requirements of this objective.
		 *
		 * If this requirement is inapplicable to the passed event, `true` should be returned
		 * unless the purpose of this requirement is to filter this kind of events.
		 */
		abstract fun isAccepted(event: ObjectiveEvent): Boolean
	}
}
