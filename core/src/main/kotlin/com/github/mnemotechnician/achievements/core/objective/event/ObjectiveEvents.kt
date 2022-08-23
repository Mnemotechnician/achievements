package com.github.mnemotechnician.achievements.core.objective.event

import com.github.mnemotechnician.achievements.core.misc.playerTeam
import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent.*
import com.github.mnemotechnician.achievements.core.world.TileIndexer
import mindustry.content.Blocks
import mindustry.game.EventType.*
import mindustry.gen.Building
import mindustry.gen.Entityc
import mindustry.gen.Unit as MindustryUnit

@Suppress("unused")
class ObjectiveEvents {
	abstract class BuildingEvent(open val building: Building) : ObjectiveEvent()

	abstract class UnitEvent(open val unit: MindustryUnit) : ObjectiveEvent()

	/** A building has been built (includes core upgrades). */
	class ConstructionEvent(building: Building) : BuildingEvent(building) {
		class Init : Listener({
			fireOnIf(BlockBuildEndEvent::class, { !breaking && tile?.build != null }) {
				ConstructionEvent(tile.build)
			}
			fireOn(CoreChangeEvent::class) {
				ConstructionEvent(core)
			}
		})
	}

	/** A building has been deconstructed */
	class DeconstructionEvent(building: Building) : BuildingEvent(building) {
		// fired externally from TileIndexer to avoid a race condition
	}

	class BuildingDestroyedEvent(building: Building) : BuildingEvent(building) {
		class Init : Listener({ fireOnIf(BlockDestroyEvent::class, { tile.build != null }) {
			BuildingDestroyedEvent(tile.build)
		} })
	}

	/**
	 * A unit has been constructed by the player's team.
	 * @param entity either a [Building] or a [MindustryUnit].
	 */
	class UnitConstructionEvent(unit: MindustryUnit, val constructor: Entityc?) : UnitEvent(unit) {
		class Init : Listener({ fireOnIf(UnitCreateEvent::class, { unit.playerTeam }) {
			UnitConstructionEvent(unit, spawner ?: spawnerUnit)
		} })
	}

	/** A unit of the player's team has been destroyed. */
	class UnitLostEvent(unit: MindustryUnit)  : UnitEvent(unit) {
		class Init : Listener({
			fireOnIf(UnitDestroyEvent::class, { unit.playerTeam }) {
				UnitLostEvent(unit)
			}
		})
	}

	/** An enemy unit has been destroyed. Not necessarily by the player team. */
	class UnitDestroyedEvent(unit: MindustryUnit) : UnitEvent(unit) {
		class Init : Listener({
			fireOnIf(UnitDestroyEvent::class, { !unit.playerTeam }) {
				UnitDestroyedEvent(unit)
			}
		})
	}
}
