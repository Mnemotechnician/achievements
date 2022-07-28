package com.github.mnemotechnician.achievements.core.objective.event

import com.github.mnemotechnician.achievements.core.objective.event.ObjectiveEvent.*
import com.github.mnemotechnician.achievements.core.util.isFair
import com.github.mnemotechnician.achievements.core.util.playerTeam
import mindustry.game.EventType.*
import mindustry.gen.Building
import mindustry.gen.Entityc
import mindustry.gen.Unit as MindustryUnit

@Suppress("unused")
class ObjectiveEvents {
	abstract class BuildingEvent(val building: Building) : ObjectiveEvent()

	abstract class UnitEvent(val unit: MindustryUnit) : ObjectiveEvent()

	/** A building has been built. */
	class ConstructionEvent(building: Building) : BuildingEvent(building) {
		class Init : Listener({ fireOnIf(BlockBuildEndEvent::class, { !breaking && tile?.build != null && isFair }) {
			ConstructionEvent(tile.build)
		} })
	}

	/** A building has been deconstructed */
	class DeconstructionEvent(building: Building) : BuildingEvent(building) {
		class Init : Listener({ fireOnIf(BlockBuildEndEvent::class, { breaking && tile?.build != null && isFair }) {
			DeconstructionEvent(tile.build)
		} })
	}

	class BuildingDestroyedEvent(building: Building) : BuildingEvent(building) {
		class Init : Listener({ fireOnIf(BlockDestroyEvent::class, { tile.build != null && isFair }) {
			BuildingDestroyedEvent(tile.build)
		} })
	}

	/**
	 * A unit has been constructed by the player's team.
	 * @param entity either a [Building] or a [MindustryUnit].
	 */
	class UnitConstructionEvent(unit: MindustryUnit, val constructor: Entityc?) : UnitEvent(unit) {
		class Init : Listener({ fireOnIf(UnitCreateEvent::class, { unit.playerTeam && isFair }) {
			UnitConstructionEvent(unit, spawner ?: spawnerUnit)
		} })
	}

	/** A unit of the player's team has been destroyed. */
	class UnitLostEvent(unit: MindustryUnit)  : UnitEvent(unit) {
		class Init : Listener({
			fireOnIf(UnitDestroyEvent::class, { unit.playerTeam && isFair }) {
				UnitLostEvent(unit)
			}
		})
	}

	/** An enemy unit has been destroyed. Not necessarily by the player team. */
	class UnitDestroyedEvent(unit: MindustryUnit)  : UnitEvent(unit) {
		class Init : Listener({
			fireOnIf(UnitDestroyEvent::class, { !unit.playerTeam && isFair }) {
				UnitDestroyedEvent(unit)
			}
		})
	}
}
