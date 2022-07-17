package com.github.mnemotechnician.achievements.mod

import arc.Events
import arc.graphics.g2d.TextureRegion
import arc.scene.style.TextureRegionDrawable
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
	}

	private fun TextureRegion.drawable() = TextureRegionDrawable(this)
}
