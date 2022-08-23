package com.github.mnemotechnician.achievements.core.world

import arc.Events
import arc.struct.Queue
import com.github.mnemotechnician.achievements.core.objective.event.*
import com.github.mnemotechnician.achievements.core.AchievementManager
import mindustry.Vars
import mindustry.game.EventType
import mindustry.game.EventType.BlockBuildBeginEvent
import mindustry.game.EventType.Trigger
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.ConstructBlock.ConstructBuild
import java.lang.ref.SoftReference

/**
 * Keeps track of buildings, both existing and removed ones (for a limited time).
 *
 * This class is not thread-safe, accessing it concurrently with the main thread may lead to undefined behaviour.
 */
object TileIndexer {
	/** All indexed buildings. May contain block that are already being deconstructed (i.e. replaced with ConstructBuild). */
	val indices = Queue<Building>()
	/**
	 * Cotains pairs of (deconstrcted building, time in ms when it will be removed).
	 * Normally, buildings are stored here for 5 seconds after their removal.
	 */
	val deconstructionIndices = Queue<DeconstructedBuilding>()

	/** Buildings to be de-indexed on the next frame. */
	private val removalSubjects = Queue<Building>()

	init {
		Events.run(Trigger.update) {
			repeat(removalSubjects.size) {
				indices.remove(removalSubjects.removeFirst())
			}
			// remove garbage-collected buildings that were under deconstruction before
			deconstructionIndices.remove { it.isInvalid() }
			// schedule the removal of invalid indices
			indices.forEach {
				if (!it.isValid) removalSubjects.add(it)
			}
		}

		Events.on(EventType.WorldLoadEvent::class.java) {
			rebuildIndices()
		}

		Events.on(BlockBuildBeginEvent::class.java) { event ->
			if (event.breaking) {
				// a building is undergoing a deconstruction
				indices.find { it.tile == event.tile }?.let { build ->
					removalSubjects.add(build)
					deconstructionIndices.add(DeconstructedBuilding(build, System.currentTimeMillis() + 5000L))
					
					AchievementManager.fireEvent(ObjectiveEvents.DeconstructionEvent(build))
				}
			}
		}

		Events.on(EventType.BlockBuildEndEvent::class.java) { event ->
			if (!event.breaking) {
				addIndex(event.tile.build)
			}
		}
	}

	fun addIndex(building: Building) {
		indices.addLast(building)
	}

	fun removeIndex(building: Building) {
		indices.remove(building)
	}

	/**
	 * Iterates over each valid indexed block of the specified [kind], belonging to the specified [team].
	 * Null [team] / [kind] mean that the respective properties of the blocks are ignored.
	 */
	inline fun eachBuild(team: Team? = null, kind: Block? = null, action: (Building) -> Unit) {
		indices.forEach {
			if (it.isValid && (team == null || it.team == team) && (kind == null || it.block == kind)) {
				action(it)
			}
		}
	}

	/**
	 * Gets the building that was in the place of this [ConstructBuild].
	 *
	 * May return null if the said building was garbage-collected, if there was no such building
	 * (i.e. the ConstructBuild represents a new block under construction), or if the type of the building
	 * that was here was not indexed.
	 */
	fun getDeconstructedBuild(onBuild: ConstructBuild): Building? {
		return getDeconstructedBuild(onBuild.tile, onBuild.team)
	}

	/**
	 * Gets the deconstructed building that was occupying this tile before.
	 *
	 * May return null if the said building was garbage-collected, if there was no such building,
	 * if the type of the building that was here was not indexed, if the building was destroyed by
	 * any means other than deconstruction, if [team] is not null and it's not equal to the team
	 * of the building, etc.
	 */
	fun getDeconstructedBuild(tile: Tile, team: Team? = null): Building? {
		return deconstructionIndices.find {
			val b = it.get() ?: return@find false
			b.tile == tile && (team == null || b.team == team)
		}?.get()
	}

	/** Performs a full re-indexing, clearing [indices] and iterating over every building on the map to populate it. */
	fun rebuildIndices() {
		//Time.mark()

		indices.clear()
		removalSubjects.clear()
		Vars.indexer.eachBlock(null, 0f, 0f, Float.MAX_VALUE, { true }) {
			indices.add(it)
		}

		//Log.info("TileIndexer: indices rebuilt: took ${Time.elapsed()} ms.")
	}

	/**
	 * A building that was deconstructed but is still being in the index for some time.
	 * Should be garbage-collected when currentTimeMillis >= aliveUntil.
	 */
	class DeconstructedBuilding(building: Building, val aliveUntil: Long) {
		val building = SoftReference(building)

		fun get() = building.get()
		fun isInvalid() = building.get() == null || System.currentTimeMillis() >= aliveUntil
	}
}
