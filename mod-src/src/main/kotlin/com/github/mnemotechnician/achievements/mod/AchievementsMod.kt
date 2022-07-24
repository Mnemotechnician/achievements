package com.github.mnemotechnician.achievements.mod

import arc.Events
import arc.scene.ui.layout.Table
import arc.util.Log
import com.github.mnemotechnician.achievements.core.Achievement.AchievementUnlockEvent
import com.github.mnemotechnician.achievements.mod.content.CoreAchievements
import com.github.mnemotechnician.achievements.gui.AchievementTreeDialog
import com.github.mnemotechnician.mkui.extensions.dsl.imageButton
import com.github.mnemotechnician.mkui.extensions.elements.findOrNull
import mindustry.Vars
import mindustry.game.EventType
import mindustry.gen.Icon
import mindustry.mod.Mod

class AchievementsMod : Mod() {
	val achievementTree by lazy { AchievementTreeDialog() }

	init {
		Events.on(EventType.ClientLoadEvent::class.java) {
			CoreAchievements.load()
			addHudButton()
		}

		// todo temporary solution
		Events.on(AchievementUnlockEvent::class.java) {
			Vars.ui.hudfrag.showToast("Achievement unlocked: ${it.achievement.displayName}!")
		}
	}

	fun addHudButton() {
		val target = Vars.ui.hudGroup.findOrNull<Table>("overlaymarker").let {
			it?.findOrNull<Table>("mobile buttons") ?: it
		} ?: run {
			Log.err("Skill issue detected: nowhere to put the achievement dialog button.")
			return
		}

		// todo create an icon
		target.imageButton(Icon.none) {
			achievementTree.show()
		}.fill()
	}
}
