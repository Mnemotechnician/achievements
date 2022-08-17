package com.github.mnemotechnician.achievements.core.objective.impl

import arc.util.Time
import com.github.mnemotechnician.achievements.core.misc.emojiOrName
import com.github.mnemotechnician.achievements.core.objective.AbstractCounterObjective
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvents
import com.github.mnemotechnician.achievements.core.world.TileIndexer
import mindustry.Vars
import mindustry.gen.Building
import mindustry.world.Block

/**
 * Requires the player to **own** [targetCount] blocks of the specified kind on the map.
 * This means that not only newly constructed blocks are counted, but also all existing ones.
 * This is useful when you want the player to have a building that cannot be constructed twice,
 * e.g. a core, or to have a building with complex requirements that cannot be met during construction,
 * e.g. a meltdown filled with cryofluid.
 *
 * This class uses [TileIndexer] and updates once a second to optimise the process, however, it's
 * still a very performance-heavy objective. Whenever possible, [BuildBlocksObjective] should be used
 * instead.
 *
 * Additionally, this objective doesn't update when it's parent achievement is already completed.
 */
open class OwnBlocksObjective(
	targetCount: Int,
	vararg val kinds: Block
) : AbstractCounterObjective(targetCount, "own-blocks", NO_EVENTS) {
	protected var lastUpdate = 0L

	val kindsDescription by lazy { kinds.joinToString(", ") { it.emojiOrName() } }

	init {
		require(kinds.isNotEmpty()) { "OwnBlocksObjective.kinds must not be empty." }

		updates = true
		kinds.forEach { TileIndexer.indexBlock(it) }
	}

	override fun modifyBundleParams(list: MutableList<() -> Any?>) {
		list.add(0) { kindsDescription }
	}

	// ignored
	override fun handleEvent(event: ObjectiveEvent) { }
	override fun receiveEvent(event: ObjectiveEvent) = false

	override fun update() {
		if (!parent.isCompleted) {
			if (Time.millis() - lastUpdate >= 1000L) {
				lastUpdate = Time.millis()

				count = 0
				TileIndexer.eachBuild(Vars.player.team(), null) {
					if (it.block in kinds && isAccepted(it)) count++
				}

				parent.update(false) // update the parent without waiting for the next event
			}
		} else {
			// the achievement is already completed, so we just pretend this objective is too.
			count = targetCount
		}
	}

	/**
	 * Passes [testBuildingEvent] with the providen [building] to [AbstractCounterObjective.isAccepted]
	 * to check if the filters and requirements accept this building.
	 */
	protected open fun isAccepted(building: Building): Boolean {
		if (tempBuildEvent == null) {
			tempBuildEvent = ReusableBuildEvent(building)
		} else 
			tempBuildEvent!!.building = building
		}
		return isAccepted(tempBuildEvent!!)
	}

	/** Reusable BuildingEvent. Used internally, see [isAccepted]. */
	class ReusableBuildEvent(build: Building) : ObjectiveEvents.BuildingEvent(build) {
		override lateinit var building: Building
	}

	companion object {
		/** See [isAccepted]. */
		protected var tempBuildEvent: ReusableBuildEvent? = null
	}
}
