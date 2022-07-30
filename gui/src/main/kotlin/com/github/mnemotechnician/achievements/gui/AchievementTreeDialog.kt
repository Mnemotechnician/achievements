package com.github.mnemotechnician.achievements.gui

import arc.Events
import arc.graphics.Color
import arc.scene.Action
import arc.scene.Scene
import arc.scene.ui.Dialog
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.AchievementManager
import com.github.mnemotechnician.achievements.core.util.isFair
import com.github.mnemotechnician.achievements.gui.util.Bundles
import com.github.mnemotechnician.mkui.extensions.dsl.*
import com.github.mnemotechnician.mkui.extensions.elements.scaleFont
import mindustry.gen.Icon
import mindustry.ui.Styles

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
			addLabel({ achievementsTitle }).color(AStyles.accent.mul(1.25f)).growX().row()
			hsplitter(AStyles.accent, padBottom = 0f)
		}

		cont.addStack {
			add(treePane)
			// unfair game mode warning
			addTable {
				center().bottom().addTable(Styles.black5) {
					addImage(Icon.warning).color(Color.red).marginRight(10f)
					addLabel(Bundles.unfairGame, wrap = true).color(Color.red).scaleFont(1.2f).growX()
				}.pad(5f).visible { !isFair }
			}
			// search bar
			addTable {
				top().right()
				// todo: a search bar
			}
		}.grow()

		Events.on(Achievement.AchievementUnlockEvent::class.java) {
			isInvalid = true
		}
	}

	override fun draw() {
		if (isInvalid) treePane.rebuild()
		super.draw()
	}

	override fun show(stage: Scene?, action: Action?): Dialog {
		isInvalid = true
		return super.show(stage, action)
	}
}
