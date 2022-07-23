package com.github.mnemotechnician.achievements.mod.content

import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.achievement
import com.github.mnemotechnician.achievements.core.dsl.rootAchievement
import com.github.mnemotechnician.achievements.core.objective.impl.BuildBlocksObjective
import mindustry.content.Blocks
import mindustry.content.UnitTypes

object CoreAchievements {
	lateinit var root: Achievement

	fun load() {
		root = rootAchievement("beginning", Blocks.conveyor.region) {
			+ BuildBlocksObjective(1, Blocks.conveyor)

			achievement("enemies-coming", UnitTypes.dagger.region) {
				+ BuildBlocksObjective(10, Blocks.copperWall, Blocks.copperWallLarge)
				+ BuildBlocksObjective(2, Blocks.duo)
			}
		}
	}
}
