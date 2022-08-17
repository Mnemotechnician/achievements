package com.github.mnemotechnician.achievements.core.world

import arc.Events
import arc.struct.Queue
import arc.util.Log
import arc.util.Time
import com.github.mnemotechnician.achievements.core.world.TileIndexer.indexBlock
import mindustry.Vars
import mindustry.game.EventType
import mindustry.game.EventType.BlockBuildBeginEvent
import mindustry.game.EventType.Trigger
import mindustry.game.Team
import mindustry.gen.Building
import mindustry.world.Block
import mindustry.world.Tile
import mindustry.world.blocks.ConstructBlock.ConstructBuild
import java.lang.ref.WeakReference

/**
 * Keeps track of buildings of specific types.
 * In order for this indexer to begin automatically indexing a block type, [indexBlock] must be called.
 *
 * This class is not thread-safe, accessing it concurrently with the main thread may lead to undefined behaviour.
 */
object TileIndexer {
	val indexedTypes = HashSet<Block>()

	/** All indexed buildings. May contain block that are already being deconstructed (i.e. replaced with ConstructBuild). */
	val indices = Queue<Building>()
	// todo mag not be a great idea to use weak refs, since it's unknown when a building is garbage-collected
	/**
	 * Weak references to all blocks under deconstruction.
	 * May contain garbage-collected references.
	 */
	val deconstructionIndices = Queue<WeakReference<Building>>()

	/** Buildings to be de-indexed on the next frame. */
	private val removalSubjects = Queue<Building>()

	init {
		Events.run(Trigger.update) {
			repeat(removalSubjects.size) {
				indices.remove(removalSubjects.removeFirst())
			}
			// remove garbage-collected buildings that were under deconstruction before
			deconstructionIndices.remove { it.get() == null }
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
					deconstructionIndices.add(WeakReference(build))
				}
			}
		}

		Events.on(EventType.BlockBuildEndEvent::class.java) { event ->
			if (event.breaking) {
				// a building has been successfully deconstructed
				deconstructionIndices.remove { it.get()?.tile == event.tile }
				indices.remove { it.tile == event.tile } // it shouldn't be there but whatever
			} else {
				addIndex(event.tile.build)
			}
		}
	}

	fun addIndex(building: Building) {
		if (building.block !in indexedTypes) return
		indices.addLast(building)
	}

	fun removeIndex(building: Building) {
		indices.remove(building)
	}

	/**
	 * Iterates over each valid indexed block of the specified [kind], belonging to the specified [team].
	 * Null [team] / [kind] mean that the respective properties of the blocks are ignored.
	 *
	 * @throws IllegalArgumentException if [kind] is not indexed ([indexBlock] hasn't been called for it).
	 */
	inline fun eachBuild(team: Team? = null, kind: Block? = null, action: (Building) -> Unit) {
		require(kind == null || kind in indexedTypes) { "$kind is not indexed." }

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
		Time.mark()

		indices.clear()
		removalSubjects.clear()
		if (indexedTypes.isNotEmpty()) {
			Vars.indexer.eachBlock(null, 0f, 0f, Float.MAX_VALUE, { it.block in indexedTypes }) {
				indices.add(it)
			}
		}

		Log.info("TileIndexer: indices rebuilt: took ${Time.elapsed()} ms.")
	}

	/**
	 * Makes the indexer index this type of block, but does not rebuild the indexes.
	 * Must be called before using [eachBuild] with [block] as the kind.
	 *
	 * This method is expected to be called before loading any map; if it has to be called after loading,
	 * [rebuildIndices] should be called afterwards to index existing blocks of this type.
	 */
	fun indexBlock(block: Block) {
		indexedTypes.add(block)
	}
}
