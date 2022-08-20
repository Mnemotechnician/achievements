package com.github.mnemotechnician.achievements.core.objective.impl

import com.github.mnemotechnician.achievements.core.StateManager.state
import com.github.mnemotechnician.achievements.core.misc.emojiOrName
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveNotifications.ItemsChangeNotification
import com.github.mnemotechnician.mkui.delegates.dynamicBundle
import mindustry.Vars
import mindustry.type.Item
import kotlin.math.max

/**
 * Requires the player to get the specified amount of the specified item in the core storage
 * or to fill it with the specified item (if [maxCapacity] is true).
 *
 * In the latter case, even if the storage space of player decreases,
 * the required amount of items remains the same to prevent abusing.
 *
 * Additionally, once [targetCount] is reached, the objective remains completed,
 * because item count is a very volatile thing. If [maxCapacity] is true,
 * the core capacity the player had before the completion of the achievement
 * is saved and persisted regardless of the current capacity.
 */
open class CollectItemsObjective : Objective {
	/** The amount of items the player has to collect. */
	var targetCount = 0
		protected set
	/** The amount of the specified item the user has had the last time it was checked. */
	protected var lastCount = 0
	lateinit var item: Item
		private set
	/** If true, [targetCount] is set to the maximum capacity of the core. */
	val maxCapacity: Boolean

	override val description by dynamicBundle({ bundleName }, { item.emojiOrName() }, { lastCount }, { targetCount })

	override var isFulfilled by state(false) { uniqueName }
	/**
	 * If is [isFulfilled] and [maxCapacity] are true,
	 * stores the last amount of items the user was required to collect to complete the objective.
	 * Otherwise, it's meaningless.
	 */
	var fulfilledCount by state(0) { uniqueName }

	init {
		updates = true
	}

	/** Collect [count] of [item]. */
	constructor(count: Int, item: Item) : super("collect-items", Companion.acceptedEvents) {
		maxCapacity = false
		this.targetCount = count
		this.item = item
	}

	/** Fill the core with [item]. */
	constructor(item: Item) : super("collect-items", Companion.acceptedEvents) {
		maxCapacity = true
		this.item = item
		updateCount()
	}

	override fun init() {
		super.init()
		updateCount()
	}

	override fun reset() {
		isFulfilled = false
	}

	/** Updates [lastCount] and, if [maxCapacity] is true, [targetCount]. */
	protected open fun updateCount() {
		if (!isFulfilled) {
			lastCount = Vars.player.team().items().get(item)

			if (maxCapacity) {
				var newCount = 0
				Vars.player?.team()?.cores()?.each {
					newCount += it.storageCapacity
				}
				targetCount = max(targetCount, newCount)
			}
		} else {
			// it doesn't matter how many the player has now, it's already completed, so we just mimic it.
			if (maxCapacity) targetCount = fulfilledCount
			lastCount = targetCount
		}
	}

	override fun handleEvent(event: ObjectiveEvent) {
		if (event is ItemsChangeNotification) {
			updateCount()
			if (event[item] >= targetCount) {
				isFulfilled = true
				// with maxCapacity == true, targetAmount is dynamic, so we need to remember it.
				if (maxCapacity) fulfilledCount = targetCount
			}
		}
	}

	override fun update() {
		updateCount()
	}

	companion object {
		val acceptedEvents = setOf(ItemsChangeNotification::class.java)
	}
}
