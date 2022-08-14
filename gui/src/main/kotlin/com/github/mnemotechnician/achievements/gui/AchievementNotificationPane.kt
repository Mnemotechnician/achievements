package com.github.mnemotechnician.achievements.gui

import arc.graphics.Color
import arc.math.Interp
import arc.scene.actions.Actions
import arc.scene.actions.Actions.*
import arc.scene.event.*
import arc.scene.style.Drawable
import arc.scene.ui.layout.*
import arc.struct.Queue
import arc.util.*
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.gui.misc.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import com.github.mnemotechnician.mkui.extensions.elements.scaleFont
import mindustry.gen.Icon
import mindustry.gen.Tex
import mindustry.ui.Styles

/**
 * Displays notifications.
 * Primarily designed to display achievement unlock notifications,
 * but can be used to display other kinds too.
 *
 * @param achievementTree if not null, will be used to show the player the completed achievement.
 */
open class AchievementNotificationPane(
	val achievementTree: AchievementTreeDialog? = null
) : WidgetGroup() {
	/** The time for which notifications are shown, in seconds. */
	var visibilityTime = 5f
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
				notification.actions(
					moveTo(width + notification.width, notification.y, 1f, Interp.exp10In),
					Actions.remove()
				)
				currentNotification = null
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

		notification.align(Align.topRight)
		notification.validate()
		notification.pack()
		notification.setPosition(width / 2f - notification.width / 2f, height, Align.bottomLeft)
		notification.actions(
			alpha(0f),
			parallel(
				alpha(1f, 0.3f, Interp.pow3In),
				moveBy(0f, -notification.height, 1f, Interp.swingOut)
			)
		)
	}

	/** Text notification, not necessarily an achievement-related one. */
	open class Notification(
		val icon: Drawable,
		val title: String,
		val description: String?
	) : Table(Tex.button) {
		init {
			addImage(icon, scaling = Scaling.fill).size(96f).top()
			vsplitter(Color.gray).width(2f)
			addTable {
				top()
				addLabel(title).scaleFont(1.1f).growX()

				if (description != null) {
					var collapser: Collapser? = null
					// show / hide button on the title row
					imageButton({ if (collapser?.isCollapsed == true) Icon.down else Icon.up }, Styles.clearNonei) {
						collapser?.toggle()
					}.row()
					// collapser
					addCollapser(true) {
						buildContent(this)
					}.with { collapser = it }.colspan(2).growX()
				}
			}.growX()
		}

		/** Builds the content table of this notification. By default, a description label. */
		protected open fun buildContent(target: Table) {
			target.addLabel(description.orEmpty(), wrap = true).scaleFont(0.9f).growX()
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
				}.growY()
			}
		}
	}
}
