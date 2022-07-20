package com.github.mnemotechnician.achievements.mod

import arc.Events
import com.github.mnemotechnician.achievements.mod.content.CoreAchievements
import mindustry.game.EventType
import mindustry.mod.Mod

class AchievementsMod : Mod() {
	init {
		Events.on(EventType.ClientLoadEvent::class.java) {
			CoreAchievements.load()
		}
	}
}
