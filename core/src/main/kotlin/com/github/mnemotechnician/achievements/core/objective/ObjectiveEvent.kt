package com.github.mnemotechnician.achievements.core.objective

import arc.Events
import arc.util.Log
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.objective.ObjectiveEvent.Listener
import mindustry.Vars
import mindustry.game.EventType.BlockBuildEndEvent
import mindustry.gen.Building
import kotlin.reflect.KClass
import mindustry.gen.Unit as MindustryUnit

// TODO(?)
// this reflective approach may indeed simplify everything,
// but maybe i should just write an annotation processor?

/**
 * Represents an in-game event that achievement objectives rely on.
 *
 * Every subclass should have a subclass that extends the [Listener] class and has no constructor parameters.
 * It will be initialised during the first initialisation of the event, allowing it to set up event listeners.
 */
abstract class ObjectiveEvent {
	/** A building has been built. */
	class ConstructionEvent(val build: Building) : ObjectiveEvent() {
		class Init : Listener({ fireOn(BlockBuildEndEvent::class) {
			computeIf(!breaking && tile?.build != null && unit.isThePlayer) { ConstructionEvent(tile.build) }
		} })
	}

	/** A building has been deconstructed. */
	class DeconstructionEvent(val building: Building) : ObjectiveEvent() {
		class Init : Listener({ fireOn(BlockBuildEndEvent::class) {
			computeIf(breaking && tile?.build != null && unit.isThePlayer) { DeconstructionEvent(tile.build) }
		} })
	}

	/** An auxiliary class, see the KDoc of [ObjectiveEvent]. */
	abstract class Listener() {
		constructor(initAction: Listener.() -> Unit) : this() {
			initAction(this)
		}

		/** Calls [AchievementManager.fireEvent]. */
		fun fire(event: ObjectiveEvent) = AchievementManager.fireEvent(event)

		/**
		 * Sets up a listener in [Events] and fires the objective event return by the lambda
		 * when the specified game event occurs.
		 */
		inline fun <C, T : ObjectiveEvent> fireOn(type: Class<C>, crossinline transform: C.() -> T?) {
			Events.on(type) { e ->
				Log.info("firing $e")
				transform(e)?.also { fire(it) }
			}
		}

		/** @see fireOn */
		inline fun <C : Any, T : ObjectiveEvent> fireOn(type: KClass<C>, crossinline transform: C.() -> T?) {
			fireOn(type.java, transform)
		}
	}
}

private val MindustryUnit?.isThePlayer get() = this != null && this == Vars.player?.unit()

inline fun <T> computeIf(condition: Boolean, block: () -> T): T? {
	return if (condition) block() else null
}
