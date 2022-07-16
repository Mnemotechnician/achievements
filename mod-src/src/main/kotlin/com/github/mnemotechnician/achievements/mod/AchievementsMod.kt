package com.github.mnemotechnician.achievements.mod

import arc.Events
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.achievement
import com.github.mnemotechnician.achievements.core.dsl.rootAchievement
import com.github.mnemotechnician.achievements.core.objective.impl.BuildBlocksObjective
import mindustry.content.Blocks
import mindustry.game.EventType.ClientLoadEvent
import mindustry.gen.Icon
import mindustry.mod.Mod

class AchievementsMod : Mod() {
	lateinit var achievement: Achievement

	init {
		Events.on(ClientLoadEvent::class.java) {
			achievement = rootAchievement("test", Icon.terminal) {
				addObjective(BuildBlocksObjective(Blocks.router, 5))

				achievement("oh") {
					addObjective(BuildBlocksObjective(Blocks.conveyor, 2))
				}

				achievement("when-the") {
					addObjective(BuildBlocksObjective(Blocks.copperWall, 10))

					achievement("auto") {}
				}
			}
		}
	}
}
