package com.github.mnemotechnician.achievements.core.objective.event

/**
 * Similar to [ObjectiveEvent], but supposed to be fired very frequently and is therefore supposed to be reused.
 *
 * The [NotificationHelper] object is used to fire object notifications,
 * instances of this class are (normally) never created directly
 */
abstract class ObjectiveNotification : ObjectiveEvent() {
	class Notifier : Listener() {

	}
}

/** Helper object for [ObjectiveNotification]. */
object NotificationHelper {
	val listeners = HashMap<Enum<*>, () -> Unit>()
	
}
