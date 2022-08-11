package com.github.mnemotechnician.achievements.core.objective.impl

import arc.scene.ui.layout.Table
import com.github.mnemotechnician.achievements.core.StateManager.state
import com.github.mnemotechnician.achievements.core.objective.Objective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveNotifications.ItemsChangeNotification
import com.github.mnemotechnician.achievements.core.misc.emojiOrName
import com.github.mnemotechnician.mkui.delegates.dynamicBundle
import mindustry.Vars
import mindustry.type.Item
import kotlin.math.max

/**
 * Requires the player to get the specified amount of the specified item in the core storage
 * or to fill it with the specified item.
 *
 * In the latter case, even if the storage space of player decreases,
 * the required amount of items remains the same to prevent abusing.
 */
open class CollectItemsObjective : Objective {
	/** The amount of items the player has to collect. */
	var targetCount = 0
		protected set
	protected var lastCount = 0
	lateinit var item: Item
		private set
	/** If true, [targetCount] is set to the maximum capacity of the core. */
	val maxCapacity: Boolean

	override val description by dynamicBundle({ bundleName }, { item.emojiOrName() }, { lastCount }, { targetCount })

	override var isFulfilled by state(false) { uniqueName }

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
		lastCount = Vars.player.team().items().get(item)

		if (maxCapacity) {
			var newCount = 0
			Vars.player?.team()?.cores()?.each {
				newCount += it.storageCapacity
			}
			targetCount = max(targetCount, newCount)
		}
	}

	override fun handleEvent(event: ObjectiveEvent) {
		if (event is ItemsChangeNotification) {
			updateCount()
			if (event[item] >= targetCount) isFulfilled = true
		}
	}

	override fun display(target: Table) {
		super.display(target)
		// this is dumb, update() method should be a thing.
		target.update { updateCount() }
	}

	companion object {
		val acceptedEvents = setOf(ItemsChangeNotification::class.java)
	}
}
