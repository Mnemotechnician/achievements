package com.github.mnemotechnician.achievements.mod.ui

import arc.Events
import arc.scene.ui.Dialog
import com.github.mnemotechnician.achievements.core.Achievement

/**
 * A dialog that allows to view the achievement tree.
 */
class AchievementTreeDialog : Dialog() {
	/** If true, the inner pane will be rebuilt the next time the dialog is drawn. */
	var isInvalid = true

	val treePane = AchievementTreePane()

	init {
		cont.add(treePane).grow()

		addCloseButton()

		Events.on(Achievement.AchievementUnlockEvent::class.java) {
			isInvalid = true
		}
	}

	override fun draw() {
		if (isInvalid) treePane.rebuild()
		super.draw()
	}
}
