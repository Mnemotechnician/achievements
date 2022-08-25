package com.github.mnemotechnician.achievements.gui

import arc.graphics.Color
import arc.math.Interp
import arc.scene.actions.Actions
import arc.scene.actions.Actions.alpha
import arc.scene.actions.Actions.moveTo
import arc.scene.event.Touchable
import arc.scene.style.Drawable
import arc.scene.ui.layout.Table
import arc.scene.ui.layout.WidgetGroup
import arc.struct.Queue
import arc.util.Align
import arc.util.Scaling
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.gui.misc.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import com.github.mnemotechnician.mkui.extensions.elements.scaleFont
import mindustry.gen.Icon
import mindustry.graphics.Pal
import mindustry.ui.Styles
import kotlin.math.min

/**
 * Displays notifications.
 * Primarily designed to display achievement unlock notifications,
 * but can be used to display other kinds too.
 *
 * @param achievementTree if not null, will be used to show the player the completed achievement.
 */
open class NotificationPane(
	val achievementTree: AchievementTreeDialog? = null
) : WidgetGroup() {
	/** The time for which notifications are shown, in seconds. */
	var visibilityTime = 4f
	/** Visibility timer for [currentNotification], in seconds. */
	protected var visibilityTimer = 0f
	val pendingNotifications = Queue<Notification>(10)
	var currentNotification: Notification? = null

	init {
		touchable = Touchable.childrenOnly
	}

	override fun act(delta: Float) {
		super.act(delta)

		val notification = currentNotification
		if (notification != null) {
			visibilityTimer -= delta
			if (visibilityTimer <= 0f) {
				// remove the notification if its time is out
				notification.touchable = Touchable.disabled
				notification.actions(
					moveTo(width + notification.width, notification.y, 1f, Interp.exp10In),
					Actions.remove()
				)
				currentNotification = null
			} else {
				// ...or update its size and position otherwise
				val time = min(visibilityTime - visibilityTimer, 1f)
				notification.validate()
				notification.pack()
				notification.setPosition(
					x + width / 2f - notification.width / 2f, 
					y + height - notification.height * Interp.smooth.apply(time)
				)
			}
		}
		if (currentNotification == null && !pendingNotifications.isEmpty) {
			showNotificationImpl(pendingNotifications.removeFirst())
		}
	}

	override fun layout() {
		currentNotification?.validate()
		super.layout()
	}

	/** Show an achievement unlock notification for the providen achievement. */
	open fun showUnlock(achievement: Achievement) {
		showNotification(AchievementNotification(achievement))
	}

	open fun showNotification(icon: Drawable?, title: String?, description: String?) {
		showNotification(Notification(icon ?: Icon.none, title.orEmpty(), description))
	}

	/** @param important if true, the achievement is added to the beginning of the queue. */
	open fun showNotification(notification: Notification, important: Boolean = false) {
		if (currentNotification == null && pendingNotifications.isEmpty) {
			showNotificationImpl(notification)
		} else {
			if (!important) {
				pendingNotifications.addLast(notification)
			} else {
				pendingNotifications.addFirst(notification)
			}
		}
	}

	protected open fun showNotificationImpl(notification: Notification) {
		visibilityTimer = visibilityTime
		currentNotification = notification
		addChild(notification)

		notification.build()
		notification.validate()
		notification.pack()
		notification.setPosition(width / 2f - notification.width / 2f, height, Align.bottomLeft)
		notification.actions(alpha(0f), alpha(1f, 0.3f, Interp.pow3In))
	}

	/** Text notification, not necessarily an achievement-related one. */
	open inner class Notification(
		val icon: Drawable,
		val title: String,
		val description: String?
	) : Table(AStyles.notificationBackground) {
		/** Called when this notification is about to be displayed. */
		open fun build() {
			top()

			addImage(icon, scaling = Scaling.fill).size(64f).top()
			vsplitter(Color.gray).width(2f)
			addTable {
				top()
				addLabel(title).scaleFont(1.1f).growX()

				if (description != null) {
					// dismiss button
					imageButton(Icon.cancel, Styles.cleari) {
						visibilityTimer = 0f
					}.color(Pal.redderDust)
					// content
					addTable {
						buildContent(this)
					}.colspan(2).growX()
				}
			}.grow()
		}

		/** Builds the content table of this notification. By default, a description label. */
		protected open fun buildContent(target: Table) {
			target.addLabel(description.orEmpty(), wrap = true).color(Pal.lightishGray).scaleFont(0.9f).growX()
		}
	}

	inner class AchievementNotification(val achievement: Achievement) : Notification(
		achievement.icon ?: Icon.none,
		"${Bundles.achievementCompleted} ${achievement.displayName}",
		achievement.description
	) {
		override fun buildContent(target: Table) {
			super.buildContent(target)

			if (achievementTree != null) {
				target.imageButton(Icon.right, Styles.clearNonei) {
					achievementTree.show()
					achievementTree.treePane.let { pane ->
						pane.allNodes.find { it.achievement == achievement }?.let { pane.traverseToNode(it) }
					}
				}.growY().bottom()
			}
		}
	}
}
