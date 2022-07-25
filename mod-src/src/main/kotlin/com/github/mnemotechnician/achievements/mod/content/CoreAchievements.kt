package com.github.mnemotechnician.achievements.mod.content

import arc.graphics.Color
import com.github.mnemotechnician.achievements.core.Achievement
import com.github.mnemotechnician.achievements.core.dsl.achievement
import com.github.mnemotechnician.achievements.core.dsl.rootAchievement
import com.github.mnemotechnician.achievements.core.objective.impl.BuildBlocksObjective
import com.github.mnemotechnician.achievements.core.objective.impl.KillUnitsObjective
import mindustry.content.Blocks
import mindustry.content.UnitTypes
import mindustry.gen.Icon

object CoreAchievements {
	lateinit var root: Achievement

	fun load() {
		root = rootAchievement("beginning", Blocks.conveyor.uiIcon) {
			+ BuildBlocksObjective(1, Blocks.conveyor)

			achievement("enemies-coming", UnitTypes.dagger.region) { //not using uiIcon for the funnies
				+ BuildBlocksObjective(10, Blocks.copperWall, Blocks.copperWallLarge)
				+ BuildBlocksObjective(2, Blocks.duo)

				achievement("kill-enemy", Icon.commandAttack.tint(Color.red)) {
					+ KillUnitsObjective(1, UnitTypes.dagger, UnitTypes.flare)
				}
			}

			achievement("begin-mining", Blocks.mechanicalDrill.uiIcon) {
				+ BuildBlocksObjective(4, Blocks.mechanicalDrill)
				+ BuildBlocksObjective(10, Blocks.conveyor)

				achievement("find-amogus", UnitTypes.flare.uiIcon) {
					+ BuildBlocksObjective(Blocks.mechanicalPump)
				}
			}

			achievement("logistics", Blocks.conveyor.uiIcon) {
				+ BuildBlocksObjective(35, Blocks.conveyor)
				+ BuildBlocksObjective(2, Blocks.router)
				+ BuildBlocksObjective(3, Blocks.junction)

				achievement("upgrades-people", Icon.up.tint(Color.green)) {
					+ BuildBlocksObjective(10, Blocks.titaniumConveyor)
				}
				achievement("better-choice", Blocks.overflowGate.uiIcon) {
					+ BuildBlocksObjective(1, Blocks.overflowGate, Blocks.underflowGate)
				}
			}
		}
	}
}
