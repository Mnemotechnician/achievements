package com.github.mnemotechnician.achievements.core.objective

import arc.Events
import arc.util.Log
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.objective.ObjectiveEvent.Listener
import kotlin.reflect.KClass

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

		/** Same as [fireOn], but fires only when [condition] returns true. */
		inline fun <C : Any, T : ObjectiveEvent> fireOnIf(
			type: Class<C>,
			crossinline condition: C.() -> Boolean,
			crossinline transform: C.() -> T?
		) {
			fireOn(type) {
				if (condition()) transform() else null
			}
		}

		/** @see fireOnIf */
		inline fun <C : Any, T : ObjectiveEvent> fireOnIf(type: KClass<C>, crossinline condition: C.() -> Boolean, crossinline transform: C.() -> T?) {
			fireOnIf(type.java, condition, transform)
		}
	}
}
