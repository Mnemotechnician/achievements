package com.github.mnemotechnician.achievements.mod.ui

import arc.Events
import arc.scene.ui.Dialog
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.mod.util.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.addLabel
import com.github.mnemotechnician.mkui.extensions.dsl.hsplitter

/**
 * A dialog that allows to view the achievement tree.
 */
class AchievementTreeDialog : Dialog() {
	/** If true, the inner pane will be rebuilt the next time the dialog is drawn. */
	var isInvalid = true

	val treePane = AchievementTreePane()

	init {
		setFillParent(true)
		closeOnBack()
		addCloseButton()

		titleTable.apply {
			val achievementsTitle by Bundles.adynamic({ AchievementManager.allAchievements.size })

			clearChildren()
			addLabel({ achievementsTitle }).color(AStyles.accent).growX().row()
			hsplitter(AStyles.accent, padBottom = 0f)
		}

		cont.add(treePane).grow()

		Events.on(Achievement.AchievementUnlockEvent::class.java) {
			isInvalid = true
		}
	}

	override fun draw() {
		if (isInvalid) treePane.rebuild()
		super.draw()
	}
}
