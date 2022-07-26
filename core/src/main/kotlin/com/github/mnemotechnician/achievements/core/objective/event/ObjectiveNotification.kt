package com.github.mnemotechnician.achievements.core.objective.event

/**
 * Similar to [ObjectiveEvent], but supposed to be fired very frequently and is therefore reused.
 */
abstract class ObjectiveNotification : ObjectiveEvent() {
	// todo
}

/** Helper object for [ObjectiveNotification]. */
object NotificationHelper {
	val listeners = HashMap<Enum<*>, () -> Unit>()
	
}
