package com.github.mnemotechnician.achievements.mod.content

import arc.graphics.g2d.TextureRegion
import arc.scene.style.TextureRegionDrawable
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.achievement
import com.github.mnemotechnician.achievements.core.dsl.rootAchievement
import com.github.mnemotechnician.achievements.core.objective.impl.BuildBlocksObjective
import mindustry.content.Blocks
import mindustry.gen.Icon

object CoreAchievements {
	lateinit var achievement: Achievement

	fun load() {
		achievement = rootAchievement("first", Icon.terminal) {
			+ BuildBlocksObjective(Blocks.router, 5)

			achievement("oh-no", Blocks.conveyor.uiIcon.drawable()) {
				+ BuildBlocksObjective(Blocks.conveyor, 2)
			}

			achievement("when-the", Blocks.copperWall.region.drawable()) {
				+ BuildBlocksObjective(Blocks.copperWall, 10)

				achievement("auto") {}
			}
		}
	}

	private fun TextureRegion.drawable() = TextureRegionDrawable(this)
}
