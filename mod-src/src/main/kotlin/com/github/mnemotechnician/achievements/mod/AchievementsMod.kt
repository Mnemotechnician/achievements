package com.github.mnemotechnician.achievements.mod

import arc.Events
import arc.scene.ui.layout.Table
import arc.util.Log
import com.github.mnemotechnician.achievements.core.Achievement.AchievementUnlockEvent
import com.github.mnemotechnician.achievements.gui.AchievementTreeDialog
import com.github.mnemotechnician.achievements.gui.NotificationPane
import com.github.mnemotechnician.achievements.mod.content.ASettings
import com.github.mnemotechnician.achievements.mod.content.CoreAchievements
import com.github.mnemotechnician.achievements.mod.gen.ASprites
import com.github.mnemotechnician.mkui.extensions.dsl.imageButton
import com.github.mnemotechnician.mkui.extensions.elements.cell
import com.github.mnemotechnician.mkui.extensions.elements.findOrNull
import mindustry.Vars
import mindustry.game.EventType
import mindustry.mod.Mod
import mindustry.ui.Styles

class AchievementsMod : Mod() {
	init {
		Events.on(EventType.ClientLoadEvent::class.java) {
			CoreAchievements.load()
			ASettings.init()
			buildHud()
		}

		Events.on(AchievementUnlockEvent::class.java) {
			notificationPane.showUnlock(it.achievement)
		}
	}

	fun buildHud() {
		val target = Vars.ui.hudGroup.findOrNull<Table>("overlaymarker").let {
			it?.findOrNull<Table>("mobile buttons") ?: it
		} ?: run {
			Log.err("Skill issue detected: nowhere to put the achievement dialog button.")
			return
		}

		target.imageButton(ASprites.iconAchievements, Styles.cleari) {
			achievementTree.show()
		}.fill().with { button ->
			// move the button
			val cell = button.cell()
			target.cells.let {
				val pos = it.indexOf(cell)
				if (pos >= 1) {
					it.remove(pos)
					it.insert(pos - 1, cell)
				}
			}
		}

		Vars.ui.hudGroup.addChild(notificationPane.also {
			it.setFillParent(true)
		})
	}

	companion object {
		val achievementTree by lazy { AchievementTreeDialog() }
		val notificationPane by lazy { NotificationPane(achievementTree) }
	}
}
