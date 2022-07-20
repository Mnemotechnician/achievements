package com.github.mnemotechnician.achievements.core.objective

import mindustry.Vars
import mindustry.game.EventType.BlockBuildEndEvent
import mindustry.gen.Building
import mindustry.gen.Unit

class ObjectiveEvents {
	/** A building has been built. */
	class ConstructionEvent(val build: Building) : ObjectiveEvent() {
		class Init : Listener({ fireOnIf(BlockBuildEndEvent::class, { !breaking && tile?.build != null && isFair }) {
			ConstructionEvent(tile.build)
		} })
	}

	/** A building has been deconstructed. */
	class DeconstructionEvent(val building: Building) : ObjectiveEvent() {
		class Init : Listener({ fireOnIf(BlockBuildEndEvent::class, { breaking && tile?.build != null && isFair }) {
			DeconstructionEvent(tile.build)
		} })
	}
}

private val Unit?.isThePlayer get() = this != null && this == Vars.player?.unit()

private val isFair get() = !Vars.state.rules.infiniteResources

inline fun <T> computeIf(condition: Boolean, block: () -> T): T? {
	return if (condition) block() else null
}
