package com.github.mnemotechnician.achievements.core.objective.event

import arc.struct.IntSeq
import mindustry.Vars
import mindustry.game.EventType.Trigger
import mindustry.type.Item

class ObjectiveNotifications {
	/** Fired on every frame. */
	object UpdateNotification : ObjectiveNotification() {
		class Init : Notifier(Trigger.update, {
			UpdateNotification
		})
	}

	/** Fired when the amount of items in the core changes. */
	object ItemsChangeNotification : ObjectiveNotification() {
		private val lastItems = IntSeq(Vars.content.items().size)
		private val items = IntSeq(Vars.content.items().size)

		/** Gets the amount of the providen item. */
		operator fun get(item: Item) = items[item.id.toInt()]
		/** Gets the amount of the providen item **before** the change, i.e. on the previous frame. */
		fun getOld(item: Item) = lastItems[item.id.toInt()]

		class Init : Notifier(Trigger.update, {
			val size = Vars.content.items().size
			if (size > items.size) {
				items.ensureCapacity(size - items.size)
				lastItems.ensureCapacity(size - lastItems.size)
			}

			(0 until size).forEach { index->
				lastItems[index] = items[index]
			}

			var changed = false
			Vars.player.team().items().each { item, count ->
				if (get(item) != count) {
					items[item.id.toInt()] = count
					changed = true
				}
			}

			ItemsChangeNotification.takeIf { changed }
		})
	}
}
