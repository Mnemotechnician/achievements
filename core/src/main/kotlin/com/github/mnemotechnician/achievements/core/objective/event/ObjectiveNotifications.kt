package com.github.mnemotechnician.achievements.core.objective.event

import mindustry.game.EventType

class ObjectiveNotifications {
	/** Fired on every frame. */
	object UpdateNotification : ObjectiveNotification() {
		class Init : Notifier(EventType.Trigger.update, {
			UpdateNotification
		})
	}
}
