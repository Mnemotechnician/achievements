package com.github.mnemotechnician.achievements.mod.content

import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.achievement
import com.github.mnemotechnician.achievements.core.dsl.rootAchievement
import com.github.mnemotechnician.achievements.core.objective.impl.BuildBlocksObjective
import mindustry.content.Blocks
import mindustry.gen.Icon

object CoreAchievements {
	lateinit var achievement: Achievement

	fun load() {
		// todo these are temoorary
		achievement = rootAchievement("first", Icon.terminal) {
			+ BuildBlocksObjective(Blocks.router, 5)

			achievement("oh-no", Blocks.conveyor.uiIcon) {
				+ BuildBlocksObjective(Blocks.conveyor, 2)
			}

			achievement("when-the", Blocks.copperWall.region) {
				+ BuildBlocksObjective(Blocks.copperWall, 10)

				achievement("auto") {}
			}
		}
	}
}
