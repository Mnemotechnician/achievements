package com.github.mnemotechnician.achievements.core.objective

import com.github.mnemotechnician.achievements.core.util.isFair
import com.github.mnemotechnician.achievements.core.util.playerTeam
import mindustry.Vars
import mindustry.game.EventType.*
import mindustry.gen.Building
import mindustry.gen.Entityc
import mindustry.gen.Unit as MindustryUnit

class ObjectiveEvents {
	/** A building has been built by the player's team. */
	class ConstructionEvent(val build: Building) : ObjectiveEvent() {
		class Init : Listener({ fireOnIf(BlockBuildEndEvent::class, { !breaking && tile?.build != null && unit.playerTeam && isFair }) {
			ConstructionEvent(tile.build)
		} })
	}

	/** A building has been deconstructed by the player's team. */
	class DeconstructionEvent(val building: Building) : ObjectiveEvent() {
		class Init : Listener({ fireOnIf(BlockBuildEndEvent::class, { breaking && tile?.build != null && unit.playerTeam && isFair }) {
			DeconstructionEvent(tile.build)
		} })
	}

	/**
	 * A unit has been constructed by the player's team.
	 * @param entity either a [Building] or a [MindustryUnit].
	 */
	class UnitConstructionEvent(val unit: MindustryUnit, val constructor: Entityc?) : ObjectiveEvent() {
		class Init : Listener({ fireOnIf(UnitCreateEvent::class, { unit.playerTeam && isFair }) {
			UnitConstructionEvent(unit, spawner ?: spawnerUnit)
		} })
	}

	/** A unit of the player's team has been destroyed. */
	class UnitLostEvent(val unit: MindustryUnit)  : ObjectiveEvent() {
		class Init : Listener({
			fireOnIf(UnitDestroyEvent::class, { unit.playerTeam && isFair }) {
				UnitLostEvent(unit)
			}
		})
	}

	/** An enemy unit has been destroyed. Not necessarily by the player team. */
	class UnitDestroyedEvent(val unit: MindustryUnit)  : ObjectiveEvent() {
		class Init : Listener({
			fireOnIf(UnitDestroyEvent::class, { !unit.playerTeam && isFair }) {
				UnitDestroyedEvent(unit)
			}
		})
	}
}
