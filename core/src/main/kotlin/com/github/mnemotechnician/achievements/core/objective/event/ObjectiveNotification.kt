package com.github.mnemotechnician.achievements.core.objective.event

import arc.Events
import com.github.mnemotechnician.achievements.core.AchievementManager

/**
 * Similar to [ObjectiveEvent], but supposed to be fired very frequently and is therefore supposed to be reused.
 *
 * The [NotificationHelper] object is used to fire object notifications,
 * instances of this class are (normally) never created directly
 */
abstract class ObjectiveNotification : ObjectiveEvent() {
	open inner class Notifier() : Listener() {
		/** Executes [initAction] upon creation. */
		constructor(initAction: Notifier.() -> Unit) : this() {
			initAction(this)
		}

		/** Begins listening for [trigger] upon creation. */
		constructor(trigger: Enum<*>, action: () -> ObjectiveNotification?) : this() {
			fireOn(trigger, action)
		}

		/** See [NotificationHelper.fireOn]. */
		fun fireOn(trigger: Enum<*>, action: () -> ObjectiveNotification?) = NotificationHelper.fireOn(trigger, action)
	}
}

/** Helper object for [ObjectiveNotification]. */
object NotificationHelper {
	val listeners = HashMap<Enum<*>, MutableList<() -> ObjectiveNotification?>>()

	/**
	 * When [trigger] is fired, executes [action].
	 * If it returns a non-null notification, that notification is fired.
	 */
	fun fireOn(trigger: Enum<*>, action: () -> ObjectiveNotification?) {
		listeners.computeIfAbsent(trigger) {
			ArrayList<() -> ObjectiveNotification?>(10).also { list ->
				Events.run(trigger) {
					list.forEach {
						it().also { if (it != null) AchievementManager.fireEvent(it) }
					}
				}
			}
		}.add(action)
	}
}
